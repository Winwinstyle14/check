package com.vhc.ec.contract.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.contract.definition.*;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.util.DateUtil;
import com.vhc.ec.contract.util.StringUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.vhc.ec.contract.entity.Base;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Document;
import com.vhc.ec.contract.entity.Field;
import com.vhc.ec.contract.entity.Participant;
import com.vhc.ec.contract.entity.Recipient;
import com.vhc.ec.contract.entity.Reference;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.repository.FieldRepository;
import com.vhc.ec.contract.repository.ParticipantRepository;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.repository.ReferenceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
public class ContractService {

    private final ContractRepository contractRepository;
    private final ReferenceRepository referenceRepository;
    private final DocumentRepository documentRepository;
    private final ParticipantRepository participantRepository;
    private final RecipientRepository recipientRepository;
    private final FieldRepository fieldRepository;
    private final CustomerService customerService;
    private final FileService fileService;
    private final RecipientService recipientService;
    private final ModelMapper modelMapper;

    /**
     * Tạo mới hợp đồng của khách hàng
     *
     * @param contractDto {@link ContractDto} Thông tin của hợp đồng cần tạo
     * @param customerId  Mã số của khách hàng
     * @return {@link ContractDto} Thông tin hợp đồng đã tạo
     */
    @Transactional
    public ContractDto create(ContractDto contractDto, int customerId) {
        var organizationDtoOptional = customerService.getOrganizationByCustomer(customerId);
        if (organizationDtoOptional.isEmpty()) {
            throw new Error(
                    String.format("can't get oragnization of customer. {\"customer_id\": %d}", customerId)
            );
        }


        var contract = modelMapper.map(contractDto, Contract.class);
        contract.setCustomerId(customerId);
        contract.setOrganizationId(organizationDtoOptional.get().getId());
        contract.setCreatedBy(customerId);
        contract.setCreatedAt(new Date());

        if(!StringUtils.hasText(contract.getContractUid())){
            contract.setContractUid(StringUtil.generatePwd(8));
        }

        final var referenceSet = Set.copyOf(
                contract.getRefs() != null ?
                        contract.getRefs() : Collections.emptyList()
        );

        contract.setStatus(ContractStatus.DRAFF);
        if (contract.getRefs() != null) {
            contract.getRefs().clear();
        }

        final var created = contractRepository.save(contract);

        // Lưu thông tin hợp đồng liên quan
        if (referenceSet.size() > 0) {
            referenceSet.forEach(reference -> reference.setContractId(created.getId()));
            referenceRepository.saveAll(referenceSet);
        }

        return modelMapper.map(created, ContractDto.class);
    }

    @Transactional
    public Optional<ContractDto> update(int id, ContractDto contractDto) {
        final var contractOptional = contractRepository.findById(id);
        if (contractOptional.isEmpty()) {
            return Optional.empty();
        }

        final var contract = contractOptional.get();
        contract.setName(contractDto.getName());
        contract.setContractNo(contractDto.getContractNo());
        contract.setTypeId(contractDto.getTypeId());
        contract.setNotes(contractDto.getNotes());
        contract.setSignTime(contractDto.getSignTime());
        contract.setUiConfig(contractDto.getUiConfig());
        contract.setAliasUrl(contractDto.getAliasUrl());
        contract.setContractExpireTime(contractDto.getContractExpireTime());

        // insert references
        referenceRepository.deleteAllByContractId(id);

        if (contractDto.getRefs() != null) {
            final var typeToken = new TypeToken<Collection<Reference>>() {
            }.getType();
            final Collection<Reference> refCollection = modelMapper.map(contractDto.getRefs(), typeToken);
            refCollection.forEach(ref -> {
                if (ref.getContractId() == null) {
                    ref.setContractId(id);
                }
            });
            referenceRepository.saveAll(refCollection);
        }

        return Optional.of(modelMapper.map(contract, ContractDto.class));
    }

    /**
     * Cập nhật trạng thái của hợp đồng
     *
     * @param id     Mã hợp đồng
     * @param status Trạng thái mới của hợp đồng
     * @return Thông tin của hợp đồng
     */
    public Optional<ContractDto> changeStatus(int id, int status, ContractChangeStatusRequest request) {
        log.info("[changeStatus] id: {}, status: {}", id, status);
        final var contractStatusOptional =
                Arrays.stream(ContractStatus.values()).filter(cs -> cs.getDbVal() == status).findFirst();

        if (contractStatusOptional.isPresent()) {
            final var contractOptional = contractRepository.findById(id);
            if (contractOptional.isPresent()) {
                final var contract = contractOptional.get();
                contract.setStatus(contractStatusOptional.get());

                if (request != null && StringUtils.hasText(request.getReason())) {
                    contract.setReasonReject(request.getReason());
                    contract.setCancelDate(new Date());
                }

                final var contractDto = modelMapper.map(
                        contractRepository.save(contract),
                        ContractDto.class
                );

                return Optional.of(contractDto);
            }
        }

        return Optional.empty();
    }

    /**
     * Bắt đầu quy trình xử lý nghiệp vụ trên hệ thống BPM
     *
     * @param id Mã hợp đồng
     * @return {@link ContractDto} Thông tin chi tiết của hợp đồng
     */
    @Transactional
    public Optional<ContractDto> startBPMWorklfow(int id) {
        final var contractOptional = contractRepository.findById(id);

        if (contractOptional.isPresent() && contractOptional.get().getStatus() == ContractStatus.DRAFF) {
            final var contract = contractOptional.get();
            contract.setStatus(ContractStatus.CREATED);

            final var updated = contractRepository.save(contract);

            return Optional.of(modelMapper.map(updated, ContractDto.class));
        }

        return Optional.empty();
    }

    /**
     * Lấy thông tin chi tiết hợp đồng theo mã hợp đồng
     *
     * @param id Mã hợp đồng
     * @return {@link ContractDto} Thông tin chi tiết của hợp đồng
     */
    public Optional<ContractDto> findById(int id) {
        final var contractOptional = contractRepository.findById(id);
        var contractDtoOptional = contractOptional.map(
                contract -> modelMapper.map(contract, ContractDto.class)
        );

        if (contractDtoOptional.isPresent()) {
            var contractDto = contractDtoOptional.get();

            var participants = contractDto.getParticipants().stream().sorted(
                    Comparator.comparingInt(ParticipantDto::getOrdering)
            ).collect(Collectors.toList());

            for (var participant : participants) {
                var recipients = participant.getRecipients().stream()
                        .collect(Collectors.toList());

                Collections.sort(recipients);
                participant.getRecipients().clear();
                participant.getRecipients().addAll(recipients);
            }

            contractDto.getParticipants().clear();
            contractDto.getParticipants().addAll(participants);

            return Optional.of(contractDto);
        }


        return Optional.empty();
    }

    public BpmnFlowRes getBpmnFlow(int id) {
        var res = new BpmnFlowRes();
        final var contractOptional = contractRepository.findById(id);
        var contractDtoOptional = contractOptional.map(
                contract -> modelMapper.map(contract, ContractDto.class)
        );

        List<BpmnRecipientDto> bpmnRecipientList = new ArrayList<>();
        res.setRecipients(bpmnRecipientList);

        if (contractDtoOptional.isPresent()) {
            var contractDto = contractDtoOptional.get();
            var customer = customerService.getCustomerById(contractDto.getCreatedBy());
            //var recipientCreated = recipientRepository.findFirstByEmailOrderByIdDesc(customer.getEmail()).orElse(null);
            var createdBy = modelMapper.map(customer, BpmnRecipientDto.class);
            res.setCreatedBy(createdBy);
            res.setReasonCancel(contractDto.getReasonReject());
            res.setCancelDate(contractDto.getCancelDate());
            res.setCreatedAt(contractDto.getCreatedAt());
            res.setContractStatus(contractDto.getStatus());

            for (var participant : contractDto.getParticipants()) {
                for (var recipient : participant.getRecipients()) {
                    var bpmnFlowRes = modelMapper.map(recipient, BpmnRecipientDto.class);
                    bpmnFlowRes.setParticipantName(participant.getName());
                    bpmnFlowRes.setParticipantOrder(participant.getOrdering());
                    bpmnFlowRes.setParticipantType(participant.getType());
                    bpmnRecipientList.add(bpmnFlowRes);
                }
            }
        }

        Collections.sort(bpmnRecipientList);

        return res;
    }

    /**
     * Tìm kiếm thông tin hợp đồng của tôi
     *
     * @param customerId Mã khách hàng
     * @param typeId     Loại hợp đồng
     * @param fromDate   Ngày tạo (từ ngày)
     * @param toDate     Ngày tạo (tới ngày)
     * @param status     Trạng thái
     * @param pageable   Thông tin phân trang
     * @return {@link PageDto <ContractDto>} Dữ liệu trả về
     */
    public PageDto<ContractDto> searchMyContract(
    		int organizationId,
            int customerId,
            Integer typeId,
            Date fromDate,
            Date toDate,
            Integer status,
            Date remainTime,
            String keyword,
            String contractNo,
            Pageable pageable) {
        try {
            contractNo = contractNo.equals("") ? null : contractNo;
            keyword =  keyword.equals("") ? null : keyword;

            final var contractPage = contractRepository.searchMyContract(
            		organizationId,
                    customerId,
                    typeId,
                    fromDate,
                    toDate,
                    status,
                    remainTime,
                    keyword,
                    contractNo,
                    pageable
            );

            return modelMapper.map(
                    contractPage, new TypeToken<PageDto<ContractDto>>() {
                    }.getType()
            );
        } catch (Exception e) {
            log.error("error: ", e);
        }

        return PageDto.<ContractDto>builder()
                .totalPages(0)
                .totalElements(0)
                .content(Collections.emptyList())
                .build();
    }
    
    public PageDto<ContractDto> searchMyContractOld(
    		int organizationId,
            int customerId, 
            Integer typeId,
            String name,
            Date fromDate,
            Date toDate,
            Integer status,
            Date remainTime,
            Pageable pageable) {
        try {
            final var contractPage = contractRepository.searchMyContractOld(
            		organizationId,
                    customerId,
                    typeId,
                    name,
                    fromDate,
                    toDate,
                    status,
                    pageable
            );

            return modelMapper.map(
                    contractPage, new TypeToken<PageDto<ContractDto>>() {
                    }.getType()
            );
        } catch (Exception e) {
            log.error(
                    String.format(
                            "can't search my contract old {\"customerId\": %d, \"typeId\": %d, \"contractNo\": \"%s\", \"fromDate\": \"%s\", \"toDate\": \"%s\", \"status\": %d, \"remainTime\": \"%s\"}",
                            customerId, typeId, name, fromDate, toDate, status, remainTime
                    ),
                    e
            );
        }

        return PageDto.<ContractDto>builder()
                .totalPages(0)
                .totalElements(0)
                .content(Collections.emptyList())
                .build();
    }

    /**
     * Tìm kiếm hợp đồng của tổ chức
     *
     * @param organizationId Mã tổ chức
     * @param typeId         Loại hợp đồng
     * @param name           Tên hợp đồng
     * @param fromDate       Ngày tạo (từ ngày)
     * @param toDate         Ngày tạo (tới ngày)
     * @param status         Trạng thái
     * @param pageable       Thông tin phân trang
     * @return {@link PageDto <ContractDto>} Dữ liệu trả về
     */
    public PageDto<ContractDto> searchMyOrgContract(
            Integer organizationId,
            Integer typeId,
            String name,
            Date fromDate,
            Date toDate,
            Integer status,
            Date remainTime,
            Pageable pageable) {
        try {
            final var contractPage = contractRepository.searchMyOrgContract(
                    organizationId,
                    typeId,
                    name,
                    fromDate,
                    toDate,
                    status,
                    remainTime,
                    pageable
            );

            return modelMapper.map(
                    contractPage, new TypeToken<PageDto<ContractDto>>() {
                    }.getType()
            );
        } catch (Exception e) {
            log.error(
                    String.format(
                            "can't search my organization contract {\"organizationId\": %d, \"typeId\": %d, \"contractNo\": \"%s\", \"fromDate\": \"%s\", \"toDate\": \"%s\", \"status\": %d, \"remainTime\": \"%s\"}",
                            organizationId, typeId, name, fromDate, toDate, status, remainTime
                    ),
                    e
            );
        }

        return PageDto.<ContractDto>builder()
                .totalPages(0)
                .totalElements(0)
                .content(Collections.emptyList())
                .build();
    }

    public PageDto<ContractDto> getShares(
            String email,
            Integer typeId,
            String name,
            String code,
            Date fromDate,
            Date toDate,
            Pageable pageable) {

        name = "".equals(name) ? null : name;
        code = "".equals(code) ? null : code;

        final var page = contractRepository.getShares(
                email,
                typeId,
                name,
                code,
                fromDate,
                toDate,
                pageable
        );

        return modelMapper.map(
                page, new TypeToken<PageDto<ContractDto>>() {
                }.getType()
        );
    }

    /**
     * Tìm kiếm hợp đồng của tổ chức mình và tổ chức con cháu
     *
     * @param orgId    Mã tổ chức
     * @param typeId   Loại hợp đồng
     * @param keyword     Tên hợp đồng
     * @param fromDate Ngày tạo (từ ngày)
     * @param toDate   Ngày tạo (tới ngày)
     * @param status   Trạng thái
     * @param remainTime
     * @param pageable Thông tin phân trang
     * @return {@link PageDto <ContractDto>} Dữ liệu trả về
     */
    public PageDto<ContractDto> findMyOrgAndDescendantContract(int orgId, Integer typeId, String keyword,
                                                               Date fromDate, Date toDate, Integer status, Date remainTime,
                                                               Pageable pageable) {

        var orgIds = new ArrayList<>(customerService.getDescendantId(orgId));
        orgIds.add(orgId);
        keyword = keyword.equals("") ? null : keyword;
        var contracts = contractRepository.findMyOrgAndDescendantContract(orgIds, typeId, keyword, fromDate, toDate,
                status, remainTime, pageable
        );

        return modelMapper.map(contracts, new TypeToken<PageDto<ContractDto>>() {
        }.getType());
    }

    /**
     * Thống kê số lượng hợp đồng do người dùng tạo
     *
     * @param customerId Mã số tham chiếu của khách hàng
     * @param fromDate   Ngày tạo hợp đồng (từ ngày)
     * @param toDate     Ngày tạo hợp đồng (tới ngày)
     * @return {@link StatisticDto}
     */
    public StatisticDto myContractStatistic(int organizationId, int customerId, Date fromDate, Date toDate) {
        final var contractCollection = contractRepository
                .searchMyContract(organizationId, customerId, fromDate, toDate);

        return parserData(contractCollection);
    }

    public StatisticDto myOrgAnDescendantStatistic(int orgId, Date fromDate, Date toDate) {

        var orgIds = new ArrayList<>(customerService.getDescendantId(orgId));
        orgIds.add(orgId);
        var contracts = contractRepository.findAllMyOrgAndDescendantContract(orgIds, null, "",
                fromDate, toDate, -1, null);


        return parserData(contracts);
    }

    public ContractStatusDto myProcessStatistic(String email, Date fromDate, Date toDate) {
        final var contractCollection = contractRepository
                .searchByEmailAddress(email, fromDate, toDate);

        long processed = 0;
        long processing = 0;
        long waiting = 0;
        long prepareExpires = 0;
        var recipients = recipientRepository.findAllByEmailAndStatus(email, RecipientStatus.PROCESSING);
        var processContractList = recipients.stream()
                .map(recipient -> recipient.getParticipant().getContractId())
                .collect(Collectors.toList());

        for (var contract : contractCollection) {
            if (contract.getStatus() == ContractStatus.SIGNED) {
                processed = processed + 1;
            } else if (contract.getStatus() == ContractStatus.PROCESSING) {
                // processing = processing + 1;

                // hợp đồng sắp quá hạn
                var now = new Date();
                var expiresDate = getExpiresDate();

                // TRUNGNQ 22/4/2022 hop dong qua han chi dem nhung hop dong den luot minh xu ly
                if (contract.getSignTime() != null
                        && contract.getSignTime().before(expiresDate) && contract.getSignTime().after(now)
                        &&processContractList.contains(contract.getId())
                ) {
                    prepareExpires = prepareExpires + 1;
                }

                // hợp đồng chờ phản hồi
                long countProcessing = 0;
                long countWaiting = 0;

                for (var participant : contract.getParticipants()) {
                    countProcessing = countProcessing + participant.getRecipients().stream()
                            .filter(recipient -> recipient.getStatus() == RecipientStatus.PROCESSING
                                    && recipient.getEmail().equals(email))
                            .count();

                    countWaiting = countWaiting + participant.getRecipients().stream().filter(
                            recipient -> (recipient.getStatus() == RecipientStatus.APPROVAL || recipient.getStatus()
                                    == RecipientStatus.REJECT)
                                    && recipient.getEmail().equals(email)
                    ).count();
                }

                if (countProcessing > 0) {
                    processing = processing + 1;

                }

                if (countWaiting > 0) {
                    waiting = waiting + 1;
                }
            }
        }


        return ContractStatusDto.builder()
                .processing(processing)
                .processed(processed)
                .prepareExpires(prepareExpires)
                .waiting(waiting)
                .build();
    }

    public StatisticDto orgContractStatistic(int orgId, Date fromDate, Date toDate) {
        final var contractCollection = contractRepository
                .searchOrgContract(orgId, fromDate, toDate);
        return parserData(contractCollection);
    }

    public PageDto<ContractDto> getMyProcessContractProcessed(String email, Pageable pageable) {
        final var page = contractRepository.getMyProcessContractProcessed(
                email, pageable
        );

        final var typeToken = new TypeToken<PageDto<ContractDto>>() {
        }.getType();
        return modelMapper.map(page, typeToken);
    }

    public PageDto<ContractDto> getMyProcessContractProcessing(String email, Pageable pageable) {
        final var page = contractRepository.getMyProcessContractProcessing(
                email, pageable
        );

        final var typeToken = new TypeToken<PageDto<ContractDto>>() {
        }.getType();
        return modelMapper.map(page, typeToken);
    }

    public PageDto<ContractDto> getMyProcessContractWating(String email, Pageable pageable) {
        final var page = contractRepository.getMyProcessContractWating(
                email, pageable
        );

        final var typeToken = new TypeToken<PageDto<ContractDto>>() {
        }.getType();
        return modelMapper.map(page, typeToken);
    }

    public PageDto<ContractDto> getMyProcessContractExpires(String email, Pageable pageable) {
        final var now = new Date();
        final var expiresDate = getExpiresDate();

        final var page = contractRepository.getMyProcessContractExpires(
                email, now, expiresDate, pageable
        );

        final var typeToken = new TypeToken<PageDto<ContractDto>>() {
        }.getType();
        return modelMapper.map(page, typeToken);
    }

    public MessageDto checkUniqueCode(String code, int orgId) {
        Optional<ContractDto> contractOptional = Optional.empty();
        final var contractList = contractRepository
                .findByContractNoAndOrganizationIdAndStatusIn(code, orgId,
                        List.of(ContractStatus.PROCESSING, ContractStatus.SIGNED));

        for (var contract : contractList) {
            var now = LocalDate.now();
            var signDate = DateUtil.convertToLocalDateViaMilisecond(contract.getSignTime());
            if (now.compareTo(signDate) <= 0) {
                return MessageDto.builder()
                        .success(false)
                        .message("contract code existed")
                        .build();
            }
        }

        return MessageDto.builder()
                .success(true)
                .message("contract code not exists")
                .build();
    }

    public Optional<ContractDto> findByName(String name) {
        final var contractOptional = contractRepository.findByName(name);

        return contractOptional.map(
                contract -> modelMapper.map(contract, ContractDto.class)
        );
    }

    @Transactional
    public Optional<ContractDto> clone(int userId, int id) {
        var contractOptional = contractRepository.findById(id);
        
        var fieldByContractId = fieldRepository.findByContractIdOrderByTypeAsc(id);
        
        if (contractOptional.isPresent()) {
            try {
                var oldContract = contractOptional.get();

                var newContract = new Contract();
                BeanUtils.copyProperties(
                        oldContract, newContract,
                        "id", "contractNo", "status", "documents",
                        "participants", "refs",
                        "createdBy", "updatedBy", "createdAt", "updatedAt", "contractUid"
                );

                // get reference id list
                List<Integer> refList = new ArrayList<>();
                if (oldContract.getRefs() != null && oldContract.getRefs().size() > 0) {
                    refList = oldContract.getRefs().stream().map(Reference::getRefId)
                            .collect(Collectors.toList());
                }

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

                // TRUNGNQ them file co type la FINALLY clone tu file PRIMARY
                for (var document: documentCollection) {
                    if (document.getType() == DocumentType.PRIMARY) {
                        var newDoc = new Document();
                        BeanUtils.copyProperties(document, newDoc);
                        newDoc.setType(DocumentType.FINALLY);
                        documentCollection.add(newDoc);
                        break;
                    }
                }

                // participant list
                List<Integer> oldParticipantIdCollection = new ArrayList<>();
                if (oldContract.getParticipants() != null && oldContract.getParticipants().size() > 0) {
                    oldParticipantIdCollection = oldContract.getParticipants()
                            .stream().map(
                                    Base::getId
                            ).collect(Collectors.toList());
                }


                // create contract
                newContract.setStatus(ContractStatus.DRAFF);
                newContract.setCreatedBy(userId);
                newContract.setUpdatedBy(userId);
                newContract.setCreatedAt(new Date());
                newContract.setUpdatedAt(new Date());
                newContract.setContractUid(StringUtil.generatePwd(8));

                var inserted = contractRepository.save(newContract);
                var newContractId = inserted.getId();

                // add new contract references
                if (refList.size() > 0) {
                    var refToInsertCollection = refList.stream().map(ref -> Reference.builder()
                            .contractId(newContractId)
                            .refId(ref)
                            .createdAt(new Date())
                            .updatedAt(new Date())
                            .createdBy(userId)
                            .updatedBy(userId)
                            .build()
                    ).collect(Collectors.toList());

                    referenceRepository.saveAll(refToInsertCollection);
                }

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
                    doc.setContractId(newContractId);
                    doc.setCreatedAt(new Date());
                    doc.setUpdatedAt(new Date());
                    doc.setCreatedBy(userId);
                    doc.setUpdatedBy(userId);
                }
                var newDocumentCollection = new ArrayList<Document>();
                var documentIterable = documentRepository.saveAll(documentCollection);
                documentIterable.iterator().forEachRemaining(newDocumentCollection::add);

                Integer newDocumentId = null;
                var newDocumentOpional = newDocumentCollection.stream().filter(
                        document -> document.getType() == DocumentType.FINALLY
                ).findFirst();
                if (newDocumentOpional.isPresent()) {
                    newDocumentId = newDocumentOpional.get().getId();
                }
                
                // Field recipentid = null đã add chưa
                boolean __isAddFieldNull = false;
                
                // add participants
                for (var oldParticipantId : oldParticipantIdCollection) {
                    var oldParticipantOptional = participantRepository.findById(oldParticipantId);
                    if (oldParticipantOptional.isPresent()) {
                        var oldParticipant = oldParticipantOptional.get();
                        var oldRecipientIdCollection = oldParticipant.getRecipients()
                                .stream()
                                .filter(r -> r.getAuthorisedBy() == null)
                                .map(
                                Base::getId
                        ).collect(Collectors.toList());

                        // add new participant
                        var newParticipant = new Participant();
                        BeanUtils.copyProperties(
                                oldParticipant, newParticipant,
                                "id",
                                "contractId",
                                "recipients", "contract"
                        );

                        newParticipant.setContractId(newContractId);

                        for (var oldRecipientId : oldRecipientIdCollection) {
                            var oldRecipientOptional = recipientRepository.findById(oldRecipientId);
                            if (oldRecipientOptional.isPresent()) {
                                var oldRecipient = oldRecipientOptional.get();
                                var newRecipient = new Recipient();
                                BeanUtils.copyProperties(
                                        oldRecipient, newRecipient,
                                        "id", "status", "fields",
                                        "signAt", "processAt", "participant",
                                        "createdAt", "createdBy",
                                        "updatedAt", "updatedBy", "password",
                                        "authorisedBy", "delegateTo"
                                );

                                // add new recipient
                                newRecipient.setStatus(RecipientStatus.DEFAULT);
                                newRecipient.setCreatedAt(new Date());
                                newRecipient.setUpdatedAt(new Date());
                                newRecipient.setCreatedBy(userId);
                                newRecipient.setUpdatedBy(userId);


                                if (!customerService.findCustomerByEmail(newRecipient.getEmail())) {
                                    newRecipient.setPassword(StringUtil.generatePwd(6));
                                }

                                newParticipant.addRecipient(newRecipient);
                            }
                        }

                        var participantInserted = participantRepository.save(newParticipant);

                        // mapping new fields and old fields
                        for (var newRecipient : participantInserted.getRecipients()) {
                            var oldRecipientOptional = oldParticipant.getRecipients().stream().filter(
                                    recipient -> recipient.getEmail().equals(newRecipient.getEmail())
                            ).findFirst();

                            if (oldRecipientOptional.isPresent()) {
                                final var newDocId = newDocumentId;
                                var oldRecipient = oldRecipientOptional.get();

                                var newFieldCollection = oldRecipient.getFields()
                                        .stream().map(oldField -> {
                                            var newField = new Field();

                                            BeanUtils.copyProperties(
                                                    oldField, newField,
                                                    "id", "value",
                                                    "documentId", "contractId",
                                                    "status", "recipient",
                                                    "createdAt", "createdBy",
                                                    "updatedAt", "updatedBy"
                                            );

                                            newField.setDocumentId(newDocId);
                                            newField.setContractId(newContractId);
                                            newField.setStatus(BaseStatus.ACTIVE);
                                            newField.setCreatedAt(new Date());
                                            newField.setUpdatedAt(new Date());
                                            newField.setCreatedBy(userId);
                                            newField.setUpdatedBy(userId);
                                            newField.setRecipientId(newRecipient.getId());

                                            return newField;
                                        }).collect(Collectors.toList());

                                // lay lai field tu nguoi duoc uy quyen
                                Integer delegateTo = oldRecipient.getDelegateTo();
                                Recipient authorizedPerson = null;
                                while (delegateTo != null) {
                                    authorizedPerson = recipientRepository.findById(delegateTo).orElse(null);
                                    delegateTo = authorizedPerson.getDelegateTo();
                                }

                                if (authorizedPerson != null) {
                                    var fields = fieldRepository.findAllByRecipientId(authorizedPerson.getId());
                                    fields = fields.stream().map(oldField -> {
                                        var newField = new Field();

                                        BeanUtils.copyProperties(
                                                oldField, newField,
                                                "id", "value",
                                                "documentId", "contractId",
                                                "status", "recipient",
                                                "createdAt", "createdBy",
                                                "updatedAt", "updatedBy"
                                        );

                                        newField.setDocumentId(newDocId);
                                        newField.setContractId(newContractId);
                                        newField.setStatus(BaseStatus.ACTIVE);
                                        newField.setCreatedAt(new Date());
                                        newField.setUpdatedAt(new Date());
                                        newField.setCreatedBy(userId);
                                        newField.setUpdatedBy(userId);
                                        newField.setRecipientId(newRecipient.getId());
                                        return newField;
                                    }).collect(Collectors.toList());

                                    newFieldCollection.addAll(fields);
                                }

                                // add field không gán người xử lý
                                if(!__isAddFieldNull) {
                                    Collection<Field> fieldCollection = new ArrayList<>();
                                    if (fieldByContractId != null && fieldByContractId.size() > 0) {
                                        fieldCollection = fieldByContractId.stream().filter(
                                                field -> (field.getRecipientId() == null || field.getRecipientId() == 0)
                                        ).collect(Collectors.toList()); 
                                    }
                                    
                                    for(Field field: fieldCollection) {
                                    	var fieldTmp = new Field();
                                        BeanUtils.copyProperties(
                                        		field, fieldTmp,
                                        		"id", "value",
                                                "documentId", "contractId",
                                                "status", "recipient",
                                                "createdAt", "createdBy",
                                                "updatedAt", "updatedBy"
                                        ); 
                                        
                                        fieldTmp.setDocumentId(newDocId);
                                        fieldTmp.setContractId(newContractId);
                                        fieldTmp.setStatus(BaseStatus.ACTIVE);
                                        fieldTmp.setCreatedAt(new Date());
                                        fieldTmp.setUpdatedAt(new Date());
                                        fieldTmp.setCreatedBy(userId);
                                        fieldTmp.setUpdatedBy(userId);

                                        newFieldCollection.add(fieldTmp);
                                    }
                                    __isAddFieldNull = true;
                                } 

                                fieldRepository.saveAll(newFieldCollection);
                            }
                        }
                    }
                }

                return Optional.of(
                        modelMapper.map(inserted, ContractDto.class)
                );
            } catch (Exception e) {
                log.error("clone contract {} failure.", id, e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }

        return Optional.empty();
    }

    public AbstractTotalDto countTotalContract(int orgId) {
        var allContracts = myOrgAnDescendantStatistic(orgId, null, null);
        return new AbstractTotalDto(allContracts.getTotalProcess() + allContracts.getTotalSigned());
    }

    public PageDto<ContractDto> getContracts(String email, String phone, int status, Pageable page) {
        var contracts = contractRepository.getContracts(email, phone, status, page);
        return modelMapper.map(
                contracts, new TypeToken<PageDto<ContractDto>>() {
                }.getType()
        );
    }

    private Date getExpiresDate() {
        var now = new Date();

        return new Date(now.getTime() + (5 * 24 * 3600000));
    }

    private StatisticDto parserData(final Collection<Contract> contractCollection) {
        // thống kê số lượng hợp đồng
        final var totalSuccess = contractCollection.stream().filter(
                contract -> contract.getStatus() == ContractStatus.SIGNED
        ).count();

        final var totalCancel = contractCollection.stream().filter(
                contract -> contract.getStatus() == ContractStatus.CANCEL
        ).count();

        final var totalReject = contractCollection.stream().filter(
                contract -> contract.getStatus() == ContractStatus.REJECTED
        ).count();

        final var totalCreated = contractCollection.stream().filter(
                contract -> contract.getStatus() == ContractStatus.CREATED
        ).count();

        final var totalDraff = contractCollection.stream().filter(
                contract -> contract.getStatus() == ContractStatus.DRAFF
        ).count();

        final var totalProcessing = contractCollection.stream().filter(
                contract -> contract.getStatus() == ContractStatus.PROCESSING
                        && contract.getSignTime() != null
                        && (new Date()).getTime() <= contract.getSignTime().getTime()
        ).count();

        final var totalExpires = contractCollection.stream().filter(
                contract -> (
                        contract.getStatus() == ContractStatus.PROCESSING
                                && contract.getSignTime() != null
                                && (new Date()).getTime() > contract.getSignTime().getTime()
                )
        ).count();

        return StatisticDto.builder()
                .totalDraff(totalDraff)
                .totalCreated(totalCreated)
                .totalCancel(totalCancel)
                .totalReject(totalReject)
                .totalSigned(totalSuccess)
                .totalProcess(totalProcessing)
                .totalExpires(totalExpires)
                .build();
    }

    /**
     * Xóa hợp đồng theo mã
     *
     * @param id Mã hợp đồng
     * @return {@link MessageDto} Nội dung thông báo
     */
    @Transactional
    public MessageDto delete(int id) {
        try {
            // Xóa thành phần tham gia
            fieldRepository.deleteByContractId(id);

            // Xóa hợp đồng trạng thái bản nháp
            final var contractOptional = findById(id);
            if (contractOptional.isPresent()) {
                var contract = contractOptional.get();
                if (contract.getStatus() == ContractStatus.DRAFF.getDbVal()) {
                    contractRepository.deleteById(id);
                }
            }
        } catch (Exception e) {
            log.error("can't delete contract id = " + id, e);

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
     * 
     * @param customerId
     * @return
     */
    public Optional<Integer> getContractProcessByUser(int customerId) {
    	final var customer = customerService.getCustomerById(customerId);
    	
    	if(customer != null) {
    		final var countContract = contractRepository.getContractProcessByUser(
            		customerId, customer.getEmail()
            ); 
            
            return Optional.of(countContract);
    	} 
    	
    	return Optional.empty();
    }
    
    /**
     * Update thông tin cho phép sau khi hoàn thành hợp đồng --> đẩy file hợp đồng lên CeCA
     * @param ceCAPush
     * @param contractId
     * @return
     */
    @Transactional
    public MessageDto updateCeCAPush(int contractId, int ceCAPush) {
        final var contractOptional = contractRepository.findById(contractId);
        
        if (contractOptional.isEmpty()) {
        	return MessageDto.builder()
                    .success(false)
                    .message("Contract does not exist.")
                    .build();
        }
        
        try {
        	final var contract = contractOptional.get();
            contract.setCeCAPush(ceCAPush);
            contract.setUpdatedAt(new Date());
            contractRepository.save(contract);
            
            return MessageDto.builder()
                    .success(true)
                    .message("Update contract success.")
                    .build();
		} catch (Exception e) {
			log.error("Can't update ceca_push :{}"+e);
		} 
        
        return MessageDto.builder()
                .success(false)
                .message("Can't update ceca_push.")
                .build();
    }
    
    /**
     * Update trạng thái đẩy file hợp đồng lên CeCA
     * @param contractId
     * @param cecaStatus
     * @return
     */
    @Transactional
    public MessageDto updateCeCAStatus(int contractId, Integer cecaStatus) {
        final var contractOptional = contractRepository.findById(contractId);
        
        if (contractOptional.isEmpty()) {
        	return MessageDto.builder()
                    .success(false)
                    .message("Contract does not exist.")
                    .build();
        }
        
        try {
        	final var contract = contractOptional.get();
            contract.setCecaStatus(cecaStatus);
            contract.setUpdatedAt(new Date());
            contractRepository.save(contract);
            
            return MessageDto.builder()
                    .success(true)
                    .message("Update contract success.")
                    .build();
		} catch (Exception e) {
			log.error("Can't update ceca_status :{}"+e);
		} 
        
        return MessageDto.builder()
                .success(false)
                .message("Can't update ceca_status.")
                .build();
    }

    public List<MyProcessResponse> listContractCanSignMulti(String platform, CustomerUser customerUser) {

        var raw =  recipientRepository
                .findAllContractCanMultiSign(customerUser.getEmail())
                .stream()
                .map(recipient -> {
                    recipient.setFields(new HashSet<>(fieldRepository.findAllByRecipientId(recipient.getId())));
                    return  recipient;
                })
                .map(recipient -> modelMapper.map(recipient, MyProcessResponse.class))
                .collect(Collectors.toList());

        List<MyProcessResponse> processList = new ArrayList<>();
        for (var p : raw) {
            var signTypeIds = p.getSignType().stream().map(s -> s.getId()).collect(Collectors.toList());
            if (signTypeIds.contains(SignType.HSM.getDbVal()) ||
                    (platform.equals("web") && signTypeIds.contains(SignType.USB_TOKEN.getDbVal()))) {

                // loai cac hop dong co o so hop dong, o text
                boolean hasTextOrContractNo = false;

                for (var field : p.getFields()) {
                    if (field.getType() == FieldType.CONTRACT_NO || field.getType() == FieldType.TEXT) {
                        hasTextOrContractNo = true;
                    }
                }
                if (!hasTextOrContractNo) {
                    processList.add(p);
                }
            }

        }

        return processList;
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

        //final var countContract = contractRepository.countContractViewByUser(customerId, email, contractId);

        final var countContract = 1;

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
