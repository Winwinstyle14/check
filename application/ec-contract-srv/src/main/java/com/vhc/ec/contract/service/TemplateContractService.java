package com.vhc.ec.contract.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.dto.ContractChangeStatusRequest;
import com.vhc.ec.contract.dto.ContractDto;
import com.vhc.ec.contract.dto.CopyFileRequest;
import com.vhc.ec.contract.dto.DocumentDto;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.PageDto;
import com.vhc.ec.contract.dto.TemplateContractDto;
import com.vhc.ec.contract.dto.TemplateContractModifileDto;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Document;
import com.vhc.ec.contract.entity.TemplateContract;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.repository.TemplateContractRepository;
import com.vhc.ec.contract.repository.TemplateFieldRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Định nghĩa các dịch vụ xử lý hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateContractService {

    private final TemplateContractRepository templateContractRepository;
    private final TemplateFieldRepository fieldRepository;
    private final CustomerService customerService;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final ContractRepository contractRepository;

    /**
     * Tạo mới hợp đồng của khách hàng
     *
     * @param contractDto {@link TemplateContractDto} Thông tin của hợp đồng cần tạo
     * @param customerId  Mã số của khách hàng
     * @return {@link ContractDto} Thông tin hợp đồng đã tạo
     */
    @Transactional
    public TemplateContractDto create(TemplateContractDto contractDto, int customerId) {
        var organizationDtoOptional = customerService.getOrganizationByCustomer(customerId);
        if (organizationDtoOptional.isEmpty()) {
            throw new Error(
                    String.format("can't get oragnization of customer. {\"customer_id\": %d}", customerId)
            );
        }

        var contract = modelMapper.map(contractDto, TemplateContract.class);
        contract.setCustomerId(customerId);
        contract.setOrganizationId(organizationDtoOptional.get().getId());

        contract.setStatus(ContractStatus.DRAFF);

        final var created = templateContractRepository.save(contract);

        return modelMapper.map(created, TemplateContractDto.class);
    }

    /**
     * Cập nhật mẫu hợp đồng của khách hàng
     */
    @Transactional
    public Optional<TemplateContractDto> update(int customerId, int id, TemplateContractDto contractDto) {
        final var contractOptional = templateContractRepository.findById(id);
        if (contractOptional.isEmpty()) {
            return Optional.empty();
        }

        final var contract = contractOptional.get();
        contract.setName(contractDto.getName());
        contract.setCode(contractDto.getCode());
        contract.setTypeId(contractDto.getTypeId());
        contract.setStartTime(contractDto.getStartTime());
        contract.setEndTime(contractDto.getEndTime());

        templateContractRepository.save(contract);

        return Optional.of(modelMapper.map(contract, TemplateContractDto.class));
    }

    /**
     * Cập nhật trạng thái của hợp đồng
     *
     * @param id     Mã hợp đồng
     * @param status Trạng thái mới của hợp đồng
     * @return Thông tin của hợp đồng
     */
    public Optional<TemplateContractDto> changeStatus(int id, int status, ContractChangeStatusRequest request) {
        final var contractStatusOptional =
                Arrays.stream(ContractStatus.values()).filter(cs -> cs.getDbVal() == status).findFirst();

        if (contractStatusOptional.isPresent()) {
            final var contractOptional = templateContractRepository.findById(id);
            if (contractOptional.isPresent()) {
                final var contract = contractOptional.get();
                contract.setStatus(contractStatusOptional.get());

                final var contractDto = modelMapper.map(
                        templateContractRepository.save(contract),
                        TemplateContractDto.class
                );

                return Optional.of(contractDto);
            }
        }

        return Optional.empty();
    }

    /**
     * Tìm kiếm thông tin hợp đồng của tôi
     *
     * @param customerId Mã khách hàng
     * @param typeId       Loại hợp đồng
     * @param name       Tên hợp đồng
     * @param pageable   Thông tin phân trang
     * @return {@link PageDto <TemplateContractDto>} Dữ liệu trả về
     */
    public PageDto<TemplateContractDto> searchMyContract(
            int customerId,
            Integer typeId,
            String name,
            Pageable pageable) {
        try {
            final var contractPage = templateContractRepository.searchMyContract(
                    customerId,
                    typeId,
                    name,
                    pageable
            );

            return modelMapper.map(
                    contractPage, new TypeToken<PageDto<TemplateContractDto>>() {
                    }.getType()
            );
        } catch (Exception e) {
            log.error(
                    String.format(
                            "can't search my contract {\"customerId\": %d, \"typeId\": %d, \"contractName\": \"%s\"}",
                            customerId, typeId, name
                    ),
                    e
            );
        }

        return PageDto.<TemplateContractDto>builder()
                .totalPages(0)
                .totalElements(0)
                .content(Collections.emptyList())
                .build();
    }

    /**
     * Lấy thông tin chi tiết hợp đồng theo mã hợp đồng
     *
     * @param id Mã hợp đồng
     * @return {@link TemplateContractDto} Thông tin chi tiết của hợp đồng
     */
    public Optional<TemplateContractDto> findById(int id) {
        final var contractOptional = templateContractRepository.findById(id);

        return contractOptional.map(
                contract -> modelMapper.map(contract, TemplateContractDto.class)
        );
    }

    /**
     * Lấy danh sách mẫu hợp đồng được chia sẻ
     *
     * @param email
     * @param name
     * @param typeId
     * @param pageable
     * @return {@link TemplateContractDto} Thông tin chi tiết của hợp đồng
     */
    public PageDto<TemplateContractDto> getShares(
            String email,
            Integer typeId,
            String name,
            Pageable pageable) {
        final var page = templateContractRepository.getShares(
                email,
                typeId,
                name,
                pageable
        );

        return modelMapper.map(
                page, new TypeToken<PageDto<TemplateContractDto>>() {
                }.getType()
        );
    }

    /**
     * Xóa mẫu hợp đồng theo mã
     *
     * @param id Mã hợp đồng
     * @return {@link MessageDto} Nội dung thông báo
     */
    @Transactional
    public MessageDto delete(int id) {
        try {
        	final var contractByTemplateId = contractRepository.findByTemplateContractId(id);
        	
        	if(contractByTemplateId.size() > 0) {
        		return MessageDto.builder()
                        .success(false)
                        .message("E02")
                        .build();
        	}else {
        		// Xóa thành phần tham gia
                fieldRepository.deleteByContractId(id);

                final var contractOptional = templateContractRepository.findById(id);
                
                // Xóa mẫu hợp đồng
                if (contractOptional.isPresent()) {
                    templateContractRepository.deleteById(id);
                }
        	} 
        } catch (Exception e) {
            log.error("can't delete contract template id = " + id, e);

            return MessageDto.builder()
                    .success(false)
                    .message("E01")
                    .build();
        }

        return MessageDto.builder()
                .success(true)
                .message("E00")
                .build();
    }

    /**
     * Kiểm tra bản ghi đã tồn tại
     *
     * @param code           Mã mẫu hợp đồng
     * @param startTime      Thời gian bắt đầu hiệu lực
     * @param endTime        Thời gian hết hiệu lực
     * @param organizationId Mã tổ chức
     */

    public Optional<MessageDto> findAllByCodeStartTimeEndTimeOrgId(String code, Date startTime, Date endTime, int organizationId, Integer contractId) {
        final var contractOptional = templateContractRepository
                .findAllByCodeStartTimeEndTimeOrgId(code, startTime, endTime, organizationId, ContractStatus.DRAFF.getDbVal().intValue(), contractId);

        if (contractOptional.isPresent()) {
            final var templateContract = contractOptional.get();

            final TemplateContractDto contractCollectionDto = modelMapper.map(templateContract, TemplateContractDto.class);

            final var customer = customerService.getCustomerById(contractCollectionDto.getCreatedBy());

            return Optional.of(
                    MessageDto.builder()
                            .success(false)
                            .message(customer.getEmail())
                            .build()
            );
        }

        return Optional.of(
                MessageDto.builder()
                        .success(true)
                        .message("template contract code not exists")
                        .build()
        );
    }
    
    @Transactional
    public Optional<Collection<DocumentDto>> clone(int userId, int templateContractId, int contractId) {
    	//Lấy thông tin hợp đồng mẫu
        var contractOptional = templateContractRepository.findById(templateContractId);
        
        if (contractOptional.isPresent()) {
            try {
                var oldContract = contractOptional.get();

                var newContract = new Contract();
                BeanUtils.copyProperties(
                        oldContract, newContract,
                        "id", "contractNo", "status", "documents",
                        "createdBy", "updatedBy", "createdAt", "updatedAt"
                );

                // document list
                Collection<Document> documentCollection = new ArrayList<>();
                if (oldContract.getDocuments() != null && oldContract.getDocuments().size() > 0) {
                    documentCollection = oldContract.getDocuments()
                            .stream().filter(doc ->
                                    (doc.getType() == DocumentType.PRIMARY ||
                                            doc.getType() == DocumentType.ATTACH)
                                    && (doc.getStatus() == BaseStatus.ACTIVE)
                            )
                            .map(doc -> {
                                var currentDocument = new Document();
                                BeanUtils.copyProperties(
                                        doc, currentDocument,
                                        "id", "contractId",
                                        "createdAt", "createdBy",
                                        "updatedAt", "updatedBy"
                                );

                                return currentDocument;
                            }).collect(Collectors.toList());
                }
                 
                Collection<Document> documentCollectionClone = new ArrayList<>();
                
                // add documents
                for (var doc : documentCollection) {
                	var copyFileRequest = CopyFileRequest.builder()
                            .bucket(doc.getBucket())
                            .filePath(doc.getPath())
                            .build();
                    var copyFileResponseOptional = fileService.copy(copyFileRequest);
                    if (copyFileResponseOptional.isPresent()) {
                        doc.setPath(copyFileResponseOptional.get().getFilePath());
                    } else {
                        throw new Exception("can't copy contract file");
                    }

                    doc.setId(null);
                    doc.setContractId(contractId);
                    doc.setCreatedAt(new Date());
                    doc.setUpdatedAt(new Date());
                    doc.setCreatedBy(userId);
                    doc.setUpdatedBy(userId);
                    
                    documentCollectionClone.add(doc);
                    
                	 // clone object type = 1 
                	if(doc.getType() == DocumentType.PRIMARY) {
                		Document docTmp = new Document();
                		
                		copyFileRequest = CopyFileRequest.builder()
                                .bucket(doc.getBucket())
                                .filePath(doc.getPath())
                                .build();
                        copyFileResponseOptional = fileService.copy(copyFileRequest);
                        if (copyFileResponseOptional.isPresent()) {
                        	docTmp.setPath(copyFileResponseOptional.get().getFilePath());
                        } else {
                            throw new Exception("can't copy contract file");
                        }
                        
                        docTmp.setName(doc.getName());
                        docTmp.setBucket(doc.getBucket());
                        docTmp.setStatus(doc.getStatus());
                        docTmp.setFilename(doc.getFilename());
                        docTmp.setInternal(doc.getInternal());
                        docTmp.setOrdering(doc.getOrdering());
                        
                        docTmp.setId(null);
                        docTmp.setContractId(contractId);
                        docTmp.setCreatedAt(new Date());
                        docTmp.setUpdatedAt(new Date());
                        docTmp.setCreatedBy(userId);
                        docTmp.setUpdatedBy(userId);
                        docTmp.setType(DocumentType.FINALLY);
                        
                        documentCollectionClone.add(docTmp);
                	} 
                }
                 
                documentRepository.saveAll(documentCollectionClone); 
                
                return Optional.of(documentService.findByContract(contractId));
            } catch (Exception e) {
                log.error("clone contract {} failure.", e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }

        return Optional.empty();
    }
    
    public Optional<Collection<TemplateContractModifileDto>> getTemplateList(int customerId) {
        try {
            final var contractPage = templateContractRepository.getTemplateList(customerId);

            return Optional.of(modelMapper.map(
                    contractPage, new TypeToken<Collection<TemplateContractModifileDto>>() {
                    }.getType())
            );
        } catch (Exception e) {
            log.error(
                    String.format(
                            "can't search template {\"customerId\": %d}",
                            customerId
                    ),
                    e
            );
        }
        
        return Optional.empty();
    }

    public Collection<Contract> findByTemplateContractId(int id) {
        return contractRepository.findByTemplateContractId(id);
    }

    /**
     * Kiểm tra người đăng nhập có quyền view hợp đồng hay không?
     * @param contractId Id contract
     * @param email mail đăng nhập
     * @return
     */
    public MessageDto checkViewContract(int contractId, String email) {
        final var customer = customerService.getCustomerByEmail(email);
        var customerId = 0;

        if (customer != null) {
            customerId = customer.getId();
        }

        final var countContract = templateContractRepository.countContractViewByUser(customerId, email, contractId);

        if(countContract > 0){
            return MessageDto.builder()
                    .success(true)
                    .message("Users are allowed to view the contract.")
                    .build();
        }

        return MessageDto.builder()
                .success(false)
                .message("User is not allowed to view contract.")
                .build();
    }
}
