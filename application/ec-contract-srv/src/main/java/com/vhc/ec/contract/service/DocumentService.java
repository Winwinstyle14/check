package com.vhc.ec.contract.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.dto.ComposeDto;
import com.vhc.ec.contract.dto.DocumentDto;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Document;
import com.vhc.ec.contract.entity.Reference;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.repository.ReferenceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Quản lý thông tin tài liệu của hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final ContractRepository contractRepository;
    private final DocumentRepository documentRepository;
    private final ReferenceRepository referenceRepository;
    private final FileService fileService;
    private final ModelMapper modelMapper;

    /**
     * Thêm mới thông tin tài liệu
     *
     * @param documentDto {@link DocumentDto} Thông tin của tài liệu cần thêm mới
     * @return {@link Document} Thông tin của tài liệu vừa được tạo
     */
    public DocumentDto create(DocumentDto documentDto) {
        Document document = modelMapper.map(
                documentDto, Document.class
        );
        document.setStatus(BaseStatus.ACTIVE);

        return modelMapper.map(
                documentRepository.save(document),
                DocumentDto.class
        );
    }

    /**
     * update existed document
     *
     * @param id          id of document need to update
     * @param documentDto data about document
     * @return {@link DocumentDto}
     */
    public Optional<DocumentDto> update(int id, DocumentDto documentDto) {
        var documentOptional = documentRepository.findById(id);
        if (documentOptional.isPresent()) {
            var doc = documentOptional.get();

            var documentToUpdate = modelMapper.map(documentDto, Document.class);
            documentToUpdate.setId(id);
            documentToUpdate.setCreatedBy(doc.getCreatedBy());
            documentToUpdate.setCreatedAt(doc.getCreatedAt());

            var updated = documentRepository.save(documentToUpdate);

            return Optional.of(
                    modelMapper.map(updated, DocumentDto.class)
            );
        }

        return Optional.empty();
    }

    /**
     * Lấy danh sách tài liệu của hợp đồng
     *
     * @param contractId Mã số tham chiếu tới hợp đồng
     * @return {@link Collection<DocumentDto>} Danh sách tài liệu của hợp đồng
     */
    public Collection<DocumentDto> findByContract(int contractId) {
        Optional<Contract> contractOptional = contractRepository.findById(contractId);
        if (contractOptional.isPresent()) {
            // get document
            Collection<Document> documentCollection = documentRepository
                    .findAllByContractIdAndStatusOrderByTypeDesc(
                            contractId, BaseStatus.ACTIVE.ordinal()
                    );

            // change file path to presigned url
            final Collection<DocumentDto> documentDtoCollection = modelMapper.map(
                    documentCollection,
                    new TypeToken<Collection<DocumentDto>>() {
                    }.getType()
            );

            documentDtoCollection.forEach(doc -> {
                var path = doc.getPath();

                String presignedUrl = fileService.getPresignedObjectUrl(
                        doc.getBucket(),
                        path
                );

                doc.setInternalPath(path);
                doc.setPath(presignedUrl);
            });

            return documentDtoCollection;
        }

        return Collections.emptyList();
    }

    @Transactional
    public Optional<DocumentDto> compress(int contractId) {
        final var contractOptional = contractRepository.findById(contractId);

        if (contractOptional.isPresent()) {
            final var contract = contractOptional.get();

            // get document by contract
            final var docOptional = documentRepository
                    .findFirstByContractIdAndTypeOrderByCreatedAtDesc(
                            contractId,
                            DocumentType.COMPRESS.getDbVal()
                    );

            Document currDocument = null;

            if (docOptional.isEmpty()) {

                final var docCollection = documentRepository
                        .findAllByContractIdAndStatusOrderByTypeDesc(
                                contractId, BaseStatus.ACTIVE.ordinal()
                        );

                // find primary document
                final var docList = docCollection.stream().filter(
                        document ->
                                document.getType() == DocumentType.FINALLY 
                                || document.getType() == DocumentType.BACKUP
                        ).map(
                        document -> ComposeDto.FileDto.builder()
                                .bucket(document.getBucket())
                                .path(document.getPath())
                                .fileName(document.getFilename())
                                .build()
                ).collect(Collectors.toList());

                // find attach document
                final var attachDocList = docCollection.stream().filter(
                        document ->
                                document.getType() == DocumentType.ATTACH
                                || document.getType() == DocumentType.IMG_EKYC
                        ).map(
                        document -> ComposeDto.FileDto.builder()
                                .bucket(document.getBucket())
                                .path(document.getPath())
                                .fileName(document.getFilename())
                                .build()
                ).collect(Collectors.toList());

                // find references document
                var refCollection = referenceRepository.findAllByContractId(contractId);

                if (refCollection != null && refCollection.size() > 0) {
                    var refIds = refCollection.stream().map(
                            Reference::getContractId
                    ).collect(Collectors.toList());

                    for (int refId : refIds) {
                        final var refDoc = documentRepository.
                                findAllByContractIdAndStatusOrderByTypeDesc(
                                        refId, BaseStatus.ACTIVE.ordinal()
                                );

                        final var refPrimary = refDoc.stream().filter(
                                doc -> doc.getType() == DocumentType.FINALLY
                        ).collect(Collectors.toList());

                        for (var ref : refPrimary) {
                            var refFile = ComposeDto.FileDto.builder()
                                    .bucket(ref.getBucket())
                                    .path(ref.getPath())
                                    .fileName(ref.getFilename())
                                    .build();

                            attachDocList.add(refFile);
                        }
                    }
                }

                final var composeDto = ComposeDto.builder()
                        .bucket("rs-ec-bucket")
                        .folderName(contract.getName())
                        .fileRequestCollection(docList)
                        .attachFileRequestCollection(attachDocList)
                        .build();

                final var uploadedOptional = fileService.compose(composeDto);
                if (uploadedOptional.isPresent()) {
                    final var uploaded = uploadedOptional.get();
                    if (uploaded.isSuccess()) {
                        final var document = new Document();
                        document.setName(contract.getName());
                        document.setType(DocumentType.COMPRESS);
                        document.setPath(uploaded.getFileObject().getFilePath());
                        document.setInternal(0);
                        document.setOrdering(1);
                        document.setStatus(BaseStatus.ACTIVE);
                        document.setContractId(contractId);
                        document.setFilename(
                                String.format("%s.zip", contract.getName())
                        );
                        document.setBucket(uploaded.getFileObject().getBucket());

                        currDocument = documentRepository.save(document);
                    }
                }
            } else {
                currDocument = docOptional.get();
            }

            //
            if (currDocument != null) {
                final var documentDto = modelMapper.map(
                        currDocument,
                        DocumentDto.class
                );


                final var presignedUrl = fileService.getPresignedObjectUrl(
                        documentDto.getBucket(),
                        documentDto.getPath()
                );
                documentDto.setPath(presignedUrl);

                return Optional.of(documentDto);
            }
        }

        return Optional.empty();
    }
    
    @Transactional
    public void deleteByContractIdAndType(int contractId, int type) {
    	final var docOptional = documentRepository.findFirstByContractIdAndTypeOrderByCreatedAtDesc(
    			contractId, type);
    	
    	if(docOptional.isPresent()) {
    		final var doc = docOptional.get();
    		documentRepository.deleteById(doc.getId());;
    	}
    }
}
