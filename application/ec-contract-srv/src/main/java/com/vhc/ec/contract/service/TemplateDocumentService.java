package com.vhc.ec.contract.service;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.dto.DocumentDto;
import com.vhc.ec.contract.dto.TemplateDocumentDto;
import com.vhc.ec.contract.entity.Document;
import com.vhc.ec.contract.entity.TemplateContract;
import com.vhc.ec.contract.entity.TemplateDocument;
import com.vhc.ec.contract.repository.TemplateContractRepository;
import com.vhc.ec.contract.repository.TemplateDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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
public class TemplateDocumentService {
    private final TemplateDocumentRepository documentRepository;
    private final TemplateContractRepository contractRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;

    /**
     * Thêm mới thông tin tài liệu
     *
     * @param documentDto {@link DocumentDto} Thông tin của tài liệu cần thêm mới
     * @return {@link Document} Thông tin của tài liệu vừa được tạo
     */
    public TemplateDocumentDto create(TemplateDocumentDto documentDto) {
        TemplateDocument document = modelMapper.map(
                documentDto, TemplateDocument.class
        );
        document.setStatus(BaseStatus.ACTIVE);

        return modelMapper.map(
                documentRepository.save(document),
                TemplateDocumentDto.class
        );
    }

    /**
     * update existed document
     *
     * @param id          id of document need to update
     * @param documentDto data about document
     * @return {@link DocumentDto}
     */
    public Optional<TemplateDocumentDto> update(int id, TemplateDocumentDto documentDto) {
        var documentOptional = documentRepository.findById(id);
        if (documentOptional.isPresent()) {
            var doc = documentOptional.get();

            var documentToUpdate = modelMapper.map(documentDto, TemplateDocument.class);
            documentToUpdate.setId(id);
            documentToUpdate.setCreatedBy(doc.getCreatedBy());
            documentToUpdate.setCreatedAt(doc.getCreatedAt());

            var updated = documentRepository.save(documentToUpdate);

            return Optional.of(
                    modelMapper.map(updated, TemplateDocumentDto.class)
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
    public Collection<TemplateDocumentDto> findByContract(int contractId) {
        Optional<TemplateContract> contractOptional = contractRepository.findById(contractId);
        if (contractOptional.isPresent()) {
            // get document
            Collection<TemplateDocument> documentCollection = documentRepository
                    .findAllByContractIdAndStatusOrderByTypeDesc(
                            contractId, BaseStatus.ACTIVE.ordinal()
                    );

            // change file path to presigned url
            final Collection<TemplateDocumentDto> documentDtoCollection = modelMapper.map(
                    documentCollection,
                    new TypeToken<Collection<TemplateDocumentDto>>() {
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
}