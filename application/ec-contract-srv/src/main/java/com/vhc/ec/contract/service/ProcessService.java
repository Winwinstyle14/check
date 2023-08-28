package com.vhc.ec.contract.service;

import com.google.common.io.Files;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.contract.definition.*;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Field;
import com.vhc.ec.contract.entity.Participant;
import com.vhc.ec.contract.entity.Recipient;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.repository.FieldRepository;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.util.ByteUtil;
import com.vhc.ec.contract.util.StringUtil;
import com.vhc.ec.contract.util.VNCharacterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Định nghĩa tài nguyên xử lý hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {
    private final DocumentRepository documentRepository;
    private final FieldRepository fieldRepository;
    private final RecipientRepository recipientRepository;
    private final DocumentService documentService;
    private final FileService fileService;
    private final ModelMapper modelMapper;
    private final BpmService bpmService;
    private final CustomerService customerService;
    private final NotificationService notificationService;
    private final RecipientService recipientService;
    private final OtpService otpService;
    private final ContractRepository contractRepository; 
    
    @Value("${vhc.ec.temporary.directory}")
    private String tempFolder;

    @Value("${sign-image.otp.template}")
    private String otpTemplate;

    @Value("${vhc.ec.mobifone.sign-service.sign-api-pkcs-url-merge}")
    private String pkcsMergeUrl;

    /**
     * Uỷ quyền xử lý hợp đồng cho người dùng khác
     *
     * @param currentEmail Địa chỉ email của người uỷ quyền
     * @param newEmail     Địa chỉ email của người được uỷ quyền
     * @param fullName     Tên của người được uỷ quyền
     * @param role         Vai trò của người được uỷ quyền
     * @param recipientId  Mã tham chiếu tới thành phần tham dự
     * @param isReplace    Có giữ lại quyền của người uỷ quyền hay không?
     * @return {@link RecipientDto} Thông tin chi tiết của người dùng được uỷ quyền
     */
    @Transactional
    public MessageDto authorize(String currentEmail, String newEmail,
                                            String fullName, String phone,
                                            int role, int recipientId, boolean isReplace, String cardId) {
        final var recipientOptional = recipientRepository
                .findFirstByEmailAndRecipientIdOrderByOrderingAsc(
                        currentEmail, recipientId
                );

        // Nếu tìm thấy thông tin của người uỷ quyền
        if (recipientOptional.isPresent()) {
            final var recipient = recipientOptional.get();
            List<Recipient> authorizeList = new ArrayList<>();
            authorizeList.add(recipient);
            Integer authorisedBy = recipient.getAuthorisedBy();

            while (authorisedBy != null) {
                var r= recipientRepository.findById(authorisedBy).get();
                authorizeList.add(r);
                authorisedBy = r.getAuthorisedBy();
            }

            for (var participant : recipient.getParticipant().getContract().getParticipants()) {
                for (var r : participant.getRecipients()) {
                    if (StringUtils.hasText(cardId) && !authorizeList.contains(r)
                            && cardId.equals(r.getCardId())
                    ) {
                        return MessageDto.builder()
                                .success(false)
                                .message("Tax code has existed")
                                .build();
                    }
                }
            }

            boolean exists = customerService.findCustomerByEmail(newEmail);

            String pwd = null;
            if (!exists) {
                pwd = StringUtil.generatePwd();
            }

            // Xét 2 trường hợp:
            // 1. Nếu cần giữ lại quyền của người uỷ quyền thì thêm mới thông tin người xử lý hồ sơ
            // 2. Nếu không cần giữ lại quyền của người uỷ quyền thì cập nhật thông tin
            if (!isReplace) {
                final var toCreate = new Recipient();

                toCreate.setName(fullName);
                toCreate.setEmail(newEmail);
                toCreate.setPhone(phone);
                toCreate.setRole(recipient.getRole());
                toCreate.setOrdering(recipient.getOrdering());
                toCreate.setStatus(RecipientStatus.PROCESSING);
                toCreate.setFromAt(recipient.getFromAt());
                toCreate.setDueAt(recipient.getDueAt());
                toCreate.setSignAt(recipient.getSignAt());
                toCreate.setProcessAt(recipient.getProcessAt());
                toCreate.setSignType(recipient.getSignType());
                toCreate.setNotifyType(recipient.getNotifyType());
                toCreate.setRemind(recipient.getRemind());
                toCreate.setRemindDate(recipient.getRemindDate());
                toCreate.setRemindMessage(recipient.getRemindMessage());
                toCreate.setParticipant(recipient.getParticipant());
                toCreate.setCardId(cardId);
                toCreate.setLoginBy(recipient.getLoginBy());
                toCreate.setAuthorisedBy(recipientId);

                if (!exists) {
                    toCreate.setUsername(newEmail);
                    toCreate.setPassword(pwd);
                }

                final var created = recipientRepository.save(toCreate);
                recipient.setDelegateTo(created.getId());
                recipientRepository.save(recipient);

                // cap nhat o ky cho nguoi moi
                var fields = fieldRepository.findAllByRecipientId(recipientId);
                for (var f : fields) {
                    f.setRecipient(created);
                    f.setRecipientId(created.getId());
                }

                fieldRepository.saveAll(fields);

                // update trạng thái recipient
                recipient.setStatus(RecipientStatus.AUTHORIZE);
                recipient.setProcessAt(new Date());
                recipientRepository.save(recipient);

                // TODO: copy fields to new recipient

                // bpmn
                WorkflowDto workflowDto = WorkflowDto
                        .builder()
                        .contractId(recipient.getParticipant().getContractId())
                        .approveType(1)
                        .recipientId(recipientId)
                        .participantId(recipient.getParticipant().getId())
                        .actionType(recipient.getRole().getDbVal())
                        .build();

                bpmService.startWorkflow(workflowDto);

                // gửi thông báo đến người được ủy quyền/chuyển tiếp
                try {
                    Participant participant = created.getParticipant();
                    Contract contract = participant.getContract();
                    var customerDto = customerService.getCustomerById(contract.getCreatedBy());
                    OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerDto.getId()).get();
                    notificationService.notificationAuthorize(created, customerDto, organizationDto, contract, participant);
                } catch (Exception e) {
                    log.error("error, recipientId={}", recipient.getId(), e);
                }

            } else {
                // xác định vai trò của người xử lý hợp đồng
                var roleOptional = Arrays.stream(RecipientRole.values())
                        .filter(recipientRole -> recipientRole.getDbVal() == role)
                        .findFirst();

                recipient.setEmail(newEmail);
                recipient.setName(fullName);
                recipient.setPhone(phone);
                recipient.setCardId(cardId);
                recipient.setRole(roleOptional.orElseThrow());

                if (!exists) {
                    recipient.setUsername(newEmail);
                    recipient.setPassword(pwd);
                } else {
                    recipient.setUsername(null);
                    recipient.setPassword(null);
                }


                final var updated = recipientRepository.save(recipient);

                // gửi thông báo đến người được ủy quyền/chuyển tiếp
                try {
                    Participant participant = recipient.getParticipant();
                    Contract contract = participant.getContract();

                    var customerDto = customerService.getCustomerById(contract.getCreatedBy());

                    OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerDto.getId()).get();

                    notificationService.notificationAuthorize(recipient, customerDto, organizationDto, contract, participant);
                } catch (Exception e) {
                    log.error("error, recipientId={}", recipient.getId(), e);
                }
            }
        }

        return MessageDto.builder().success(true).build();
    }

    /**
     * Khách hàng thực hiện thao tác xử lý dữ liệu trên hợp đồng
     *
     * @param fieldId Trường dữ liệu cần xử lý
     * @return Thông tin trường dữ liệu sau khi người dùng xử lý
     */
    public Optional<FieldDto> process(int fieldId, String signInfo) {
        var fieldOptional = fieldRepository.findById(fieldId);

        if (fieldOptional.isPresent()) {
            var field = fieldOptional.get();
            var documentDtoCollection = documentService.findByContract(
                    field.getContractId()
            );

            // cần tạo bản sao của tệp tin chờ xử lý
            var isNeedCloneFile = false;

            // lấy tệp tin của hợp đồng đang được xử lý
            var docOptional = documentDtoCollection.stream()
                    .filter(documentDto -> documentDto.getType() == DocumentType.FINALLY.getDbVal())
                    .findFirst();

            // nếu hợp đồng chưa được xử lý, lấy thông tin của tệp tin mặc định trên hệ thống
            if (!docOptional.isPresent()) {
                isNeedCloneFile = true;

                docOptional = documentDtoCollection.stream()
                        .filter(documentDto -> documentDto.getType() == DocumentType.PRIMARY.getDbVal())
                        .findFirst();
            }

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();
                final var bucket = doc.getBucket();
                final var internalPath = doc.getInternalPath();

                String tempFilePath = null;
                String primaryFilePath = doc.getPath();

                // xử lý hợp đồng theo những trường hợp đã được định nghĩa
                switch (field.getType()) {
                	case CONTRACT_NO:
                    case TEXT:
                        log.info("line 291 add text: {}", field);
                        tempFilePath = addText(
	                            primaryFilePath,
	                            field.getPage(),
	                            field.getCoordinateX(),
	                            field.getCoordinateY(),
	                            field.getHeight(),
                                field.getFont(),
	                            field.getFontSize(),
	                            field.getValue(),
                                false
	                    );
	                    break;
                    case IMAGE_SIGN:

                        if (StringUtils.hasText(field.getValue())) {
                            String imgPath = fileService.getPresignedObjectUrl(
                                    bucket,
                                    field.getValue()
                            );

                            tempFilePath = addImage(
                                    "sign image",
                                    primaryFilePath,
                                    imgPath,
                                    null,
                                    field.getPage(),
                                    field.getCoordinateX(),
                                    field.getCoordinateY(),
                                    field.getWidth(),
                                    field.getHeight()
                            );
                        }

                        if (StringUtils.hasText(signInfo)) {
                            String fileName = UUID.randomUUID().toString();
                            var decodedBytes = Base64Utils.decodeFromString(signInfo);
                            File imageFile = new File(fileName);
                            try {
                                Files.write(decodedBytes, imageFile);
                                tempFilePath = addImage(
                                        "sign info",
                                        tempFilePath,
                                        null,
                                        imageFile,
                                        field.getPage(),
                                        field.getCoordinateX() + field.getWidth() - 140,
                                        field.getCoordinateY() + field.getHeight() - 40,
                                        140,
                                        40
                                );
                                FileUtils.delete(imageFile);
                            } catch (IOException e) {
                                log.error("error", e);
                            }
                        }

                        break;
                    case DIGITAL_SIGN:
                        if (StringUtils.hasText(field.getValue())) {
                            String imgPath = fileService.getPresignedObjectUrl(
                                    bucket,
                                    field.getValue()
                            );

                            tempFilePath = addImage(
                                    null,
                                    primaryFilePath,
                                    imgPath,
                                    null,
                                    field.getPage(),
                                    field.getCoordinateX(),
                                    field.getCoordinateY(),
                                    field.getWidth(),
                                    field.getHeight()
                            );
                        }
                        break;
                    default:
                        break;
                }

                // nếu xử lý trường dữ liệu trên hợp đồng thành công
                if (StringUtils.hasText(tempFilePath)) {
                    // cập nhật thông tin của tệp nội dung tới MinIO
                    Optional<UploadFileDto> uploadFileOptional = fileService.replace(
                            tempFilePath,
                            bucket,
                            !isNeedCloneFile ? internalPath : null
                    );

                    // xoá tệp tin tạm sau khi lưu trên hệ thống MinIO
                    try {
                        FileUtils.delete(new File(tempFilePath));
                    } catch (IOException e) {
                        log.error("can't delete file \"{}\"", tempFilePath);
                    }

                    if (uploadFileOptional.isPresent()) {
                        if (isNeedCloneFile) {
                            final var uploaded = uploadFileOptional.get().getFileObject();

                            // tạo mới tệp tin đang xử lý của hợp đồng
                            var docDto = DocumentDto.builder()
                                    .filename(uploaded.getFilename())
                                    .bucket(uploaded.getBucket())
                                    .path(uploaded.getFilePath())
                                    .internal(1)
                                    .ordering(1)
                                    .status(1)
                                    .type(DocumentType.FINALLY.getDbVal())
                                    .name(doc.getName())
                                    .contractId(field.getContractId())
                                    .build();

                            documentService.create(docDto);
                        }

                        return Optional.of(modelMapper.map(
                                field, FieldDto.class
                        ));
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * them o so hop dong vao file hop dong
     */
    public void addNumberContractCell(FieldDto field) {
        var documentDtoCollection = documentService.findByContract(
                field.getContractId()
        );

        // cần tạo bản sao của tệp tin chờ xử lý
        var isNeedCloneFile = false;

        // lấy tệp tin của hợp đồng đang được xử lý
        var docOptional = documentDtoCollection.stream()
                .filter(documentDto -> documentDto.getType() == DocumentType.FINALLY.getDbVal())
                .findFirst();

        // nếu hợp đồng chưa được xử lý, lấy thông tin của tệp tin mặc định trên hệ thống
        if (!docOptional.isPresent()) {
            isNeedCloneFile = true;

            docOptional = documentDtoCollection.stream()
                    .filter(documentDto -> documentDto.getType() == DocumentType.PRIMARY.getDbVal())
                    .findFirst();
        }

        if (docOptional.isPresent()) {
            final var doc = docOptional.get();
            final var bucket = doc.getBucket();
            final var internalPath = doc.getInternalPath();

            String tempFilePath = null;
            String primaryFilePath = doc.getPath();
            log.info("line 451 add text: {}", field);
            tempFilePath = addText(
                    primaryFilePath,
                    field.getPage(),
                    field.getCoordinateX(),
                    field.getCoordinateY(),
                    field.getHeight(),
                    field.getFont(),
                    field.getFontSize(),
                    field.getValue(),
                    false);

            // nếu xử lý trường dữ liệu trên hợp đồng thành công
            if (StringUtils.hasText(tempFilePath)) {
                // cập nhật thông tin của tệp nội dung tới MinIO
                Optional<UploadFileDto> uploadFileOptional = fileService.replace(
                        tempFilePath,
                        bucket,
                        !isNeedCloneFile ? internalPath : null
                );

                // xoá tệp tin tạm sau khi lưu trên hệ thống MinIO
                try {
                    FileUtils.delete(new File(tempFilePath));
                } catch (IOException e) {
                    log.error("can't delete file \"{}\"", tempFilePath);
                }

                if (uploadFileOptional.isPresent()) {
                    if (isNeedCloneFile) {
                        final var uploaded = uploadFileOptional.get().getFileObject();

                        // tạo mới tệp tin đang xử lý của hợp đồng
                        var docDto = DocumentDto.builder()
                                .filename(uploaded.getFilename())
                                .bucket(uploaded.getBucket())
                                .path(uploaded.getFilePath())
                                .internal(1)
                                .ordering(1)
                                .status(1)
                                .type(DocumentType.FINALLY.getDbVal())
                                .name(doc.getName())
                                .contractId(field.getContractId())
                                .build();

                        documentService.create(docDto);
                    }
                }
            }
        }
    }
    /**
     * Khách hàng thực hiện thao tác ký số trên hợp đồng
     *
     * @param fieldId        Mã tham chiếu tới trường dữ liệu
     * @param digitalSignDto Thông tin ký số của hợp đồng
     * @return {@link FieldDto}
     */
    @Transactional
    public Optional<FieldDto> digitalSign(int fieldId, DigitalSignDto digitalSignDto) {
        final var fieldOptional = fieldRepository.findById(fieldId);
        final var newFilePath = String.format("./tmp/%s_%s", UUID.randomUUID(), digitalSignDto.getName());
        try {
            if (fieldOptional.isPresent()) {
                final var field = fieldOptional.get();

                final var docOptional = documentRepository.findById(
                        field.getDocumentId()
                );
                if (docOptional.isPresent()) {
                    final var doc = docOptional.get();

                    final var bucket = doc.getBucket();
                    final var filePath = doc.getPath();

                    final var base64Data = digitalSignDto.getContent().split(",");
                    final var decodedBytes = Base64Utils.decodeFromString(base64Data[1]);
                    Files.write(decodedBytes, new File(newFilePath));

                    final var uploadFileDtoOptional = fileService
                            .replace(newFilePath, bucket, filePath);

                    if (uploadFileDtoOptional.isPresent()) {
                        final var recipientOptional = recipientRepository.findById(
                                field.getRecipient().getId()
                        );

                        if (recipientOptional.isPresent()) {
                            final var recipient = recipientOptional.get();
                            recipient.setStatus(RecipientStatus.APPROVAL);
                            recipient.setProcessAt(new Date());
                            recipientRepository.save(recipient);

                            field.setStatus(BaseStatus.ACTIVE);

                            FileUtils.delete(new File(newFilePath));

                            return Optional.of(
                                    modelMapper.map(
                                            fieldRepository.save(field),
                                            FieldDto.class
                                    )
                            );
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("can't write to file \"{}\"", newFilePath, e);
        }

        return Optional.empty();
    }

    @Transactional
    public void byPassContractNo(int contractId) {
        //log.info("add text: " + contractId);
        String tempFilePath = null;

        try {
            final var documentCollection = documentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                    contractId, BaseStatus.ACTIVE.ordinal()
            );

            final var docOptional = documentCollection.stream()
                    .filter(document -> document.getType() == DocumentType.FINALLY)
                    .findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();

                // lấy thông tin trường dữ liệu cần thêm
                final var fieldCollection = fieldRepository.findByContractIdOrderByTypeAsc(contractId);
                /**
                final var fieldOptional = fieldCollection.stream()
                        .filter(field -> field.getType() == FieldType.CONTRACT_NO)
                        .findFirst(); 
                
                if (fieldOptional.isPresent()) {
                    final var field = fieldOptional.get();
                    final var presignedUrl = fileService.getPresignedObjectUrl(
                            doc.getBucket(),
                            doc.getPath()
                    );

                    tempFilePath = addText(
                            presignedUrl,
                            field.getPage(),
                            field.getCoordinateX(),
                            field.getCoordinateY(),
                            field.getHeight(),
                            field.getFontSize(),
                            field.getValue()
                    );

                    log.info("tmp file: " + tempFilePath);
                    // thay thế nội dung tệp tin trên hệ thống MinIO
                    var res = fileService.replace(
                            tempFilePath, doc.getBucket(), doc.getPath()
                    );
                    log.info("replace file: " + !res.isEmpty());
                } else {
                    log.error("by pass contract no error: can't find contract field where contract_id = {}", contractId);
                }
                */
                
                final var fieldList = fieldCollection.stream()
                        .filter(field -> field.getValue() != null &&  (field.getType() == FieldType.CONTRACT_NO || field.getType() == FieldType.TEXT))
                        .collect(Collectors.toList());
                
                for(Field field : fieldList) {
            		final var presignedUrl = fileService.getPresignedObjectUrl(
                            doc.getBucket(),
                            doc.getPath()
                    );
                	
                	//log.info("==> Đường dẫn file: "+presignedUrl);
                	
            		//log.info("==> add text contract_id="+field.getContractId()+"-- type="+field.getType()+"-- value="+field.getValue());
                    log.info("line 630 add text: {}", field);
                    tempFilePath = addText(
                            presignedUrl,
                            field.getPage(),
                            field.getCoordinateX(),
                            field.getCoordinateY(),
                            field.getHeight(),
                            field.getFont(),
                            field.getFontSize(),
                            field.getValue(),
                            false
                    );
                    
                    log.info("tmp file: " + tempFilePath);
                	
                    // thay thế nội dung tệp tin trên hệ thống MinIO
                    var res = fileService.replace(
                            tempFilePath, doc.getBucket(), doc.getPath()
                    );
                    
                    log.info("replace file: " + !res.isEmpty());
                    
                   // log.info("==> add text contract_id="+field.getContractId()+" thành công");
                } 
            } else {
                log.error("by pass add text error: can't find document where contract_id = {}", contractId);
            }
        } catch (Exception e) {
            log.error("can't by pass add text: ", e);
        } finally { 
            if (StringUtils.hasText(tempFilePath)) {
                try {
                    FileUtils.deleteDirectory(new File(tempFilePath));
                } catch (Exception e) {
                    log.error("can't delete directory {}", tempFilePath);
                }
            }
        }
    }

    @Transactional
    public void byPassContractUid(int contractId) {
        String tempFilePath = null;

        try {
            // lấy thông tin hợp đồng
            final var contractOptional = contractRepository.findById(contractId);

            if(contractOptional.isPresent()){
                final var contract = contractOptional.get();

                final var documentCollection = documentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                        contractId, BaseStatus.ACTIVE.ordinal()
                );

                final var docOptional = documentCollection.stream()
                        .filter(document -> document.getType() == DocumentType.FINALLY)
                        .findFirst();

                if (docOptional.isPresent()) {
                    final var doc = docOptional.get();

                    final var presignedUrl = fileService.getPresignedObjectUrl(
                            doc.getBucket(),
                            doc.getPath()
                    );
                    log.info("line 696 add text");
                    tempFilePath = addText(
                            presignedUrl,
                            1,
                            15,
                            0,
                            25,
                            "Times New Roman",
                            11,
                            "Mã HĐ: "+contract.getContractUid(),
                            true
                    );

                    log.info("tmp file: " + tempFilePath);

                    // thay thế nội dung tệp tin trên hệ thống MinIO
                    var res = fileService.replace(
                            tempFilePath, doc.getBucket(), doc.getPath()
                    );

                    log.info("replace file: " + !res.isEmpty());
                } else {
                    log.error("by pass add text error: can't find document where contract_id = {}", contractId);
                }
            }else{
                log.error("Can't find contract_id = {}", contractId);
            }
        } catch (Exception e) {
            log.error("can't by pass add text: ", e);
        } finally {
            if (StringUtils.hasText(tempFilePath)) {
                try {
                    FileUtils.deleteDirectory(new File(tempFilePath));
                } catch (Exception e) {
                    log.error("can't delete directory {}", tempFilePath);
                }
            }
        }
    }

    /**
     * Thêm trường nội dung dạng chuỗi ký tự vào nội dung tệp tin pdf
     *
     * @param filePath  Đường dẫn tới tệp tin pdf
     * @param pageIndex Trang cần thêm nội dung
     * @param tx        Toạ độ điểm cần thêm nội dung, theo trục dọc
     * @param ty        Toạ đô điểm cần thêm nội dung, theo trục ngang
     * @param fontSize  Kích thước của định dạng
     * @param text      Nội dung cần thêm vào tệp tin pdf
     * @param isColor   Tô màu text hay không?
     * @return Đường dẫn tạm của tệp tin sau khi xử lý
     */
    private String addText(String filePath, int pageIndex, float tx, float ty, float height, String fontName, int fontSize, String text, boolean isColor) {
        log.info("add text: {} {}-{}", text, fontName, fontSize);
        try (final var pdf = Loader.loadPDF(
                new URL(filePath).openStream())) {
            final var page = pdf.getPage(pageIndex - 1);

            var pageHeight = page.getMediaBox().getHeight();

            ty = ty - ((pageIndex - 1) * pageHeight);

            final var contentStream = new PDPageContentStream(
                    pdf, page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
            );

            if(isColor){
                //set color (R, G, B) cho text
                contentStream.setNonStrokingColor(Color.blue);
            }

            // ghi dữ liệu vào tệp tin pdf
            contentStream.beginText();

            // vị trí dữ liệu bắt đầu được ghi
            contentStream.newLineAtOffset(tx, (pageHeight - ty) - height);

            fontName = (StringUtils.hasText(fontName) ? fontName : "Times New Roman");
            final var fontInputStream = new ClassPathResource(String.format("fonts/%s.ttf", fontName))
                    .getInputStream();

            final var font = PDType0Font.load(
                    pdf, fontInputStream
            );
            contentStream.setFont(font, fontSize);
            contentStream.showText(text);

            // hoàn thành ghi nội dung
            contentStream.endText();
            contentStream.close();

            final var tempFilePath = String.format(
                    "%s/%s.pdf", tempFolder,
                    UUID.randomUUID()
            );

            pdf.save(tempFilePath);

            return tempFilePath;
        } catch (IOException e) {
            log.error(String.format("can't load pdf at \"%s\"", filePath), e);
        } catch (Exception e) {
            log.error(String.format("can't write text \"%s\" to pdf at \"%s\"", text, filePath), e);
        }

        return null;
    }

    /**
     * Thêm hình ảnh vào nội dung của tệp tin pdf
     *
     * @param filePath  Đường dẫn tới tệp tin pdf
     * @param imagePath Đường dẫn tới hình ảnh cần thêm vào tệp tin pdf
     * @param pageIndex Trang cần thêm nội dung
     * @param tx        Toạ độ điểm cần thêm nội dung, theo trục dọc
     * @param ty        Toạ độ điểm cần thêm nội dung, theo trục ngang
     * @param width     Chiều dài tính từ điểm cần thêm nội dung
     * @param height    Chiều cao tính từ điểm cần thêm nội dung
     * @return Đường dẫn tạm của tệp tin sau khi xử lý
     */
    private String addImage(
            String type,
            String filePath, String imagePath,
            File imageFile,
            int pageIndex, float tx, float ty,
            float width, float height) {
        try  {

            if (type != null) {
                File tmpFile = null;
                if (type.equals("sign image")) {
                    String tmp =  String.format("%s/%s.pdf", tempFolder, UUID.randomUUID());
                    tmpFile = new File(tmp);
                    FileUtils.copyURLToFile(new URL(filePath), tmpFile);
                    filePath = tmp;
                }

                var pdfReader = new PdfReader(filePath);
                String newFilePath =  String.format("%s/%s.pdf", tempFolder, UUID.randomUUID());
                var pdfWriter = new PdfWriter(newFilePath);
                var pdfDoc = new PdfDocument(pdfReader, pdfWriter);
                log.info("newFilePath: {}" , newFilePath);

                //if (pdfDoc.getPdfVersion().compareTo(PdfVersion.PDF_1_6) < 0) {
                // FE cong chieu cao tat ca cac trang vao toa do y (y_n = page(0).h + page(1).h + .... + page(n).h)
                for (int i = 1 ;i < pageIndex; i++) {
                    ty = ty - pdfDoc.getPage(i).getPageSize().getHeight();
                }

                Image image;
                if (imageFile != null) {
                    image =  new Image(ImageDataFactory.create(imageFile.getPath()));
                } else {
                    image = new Image(ImageDataFactory.create(new URL(imagePath)));
                }

                // convert top left -> bottom left
                ty = pdfDoc.getPage(pageIndex).getPageSize().getHeight() - ty - height;
                log.info("add image at: {}, {}, {}" , pageIndex, tx, ty);
                image = image.scaleToFit(width, height);
                image.setFixedPosition(pageIndex, tx < 0 ? 0 : tx, ty < 0 ? 0 : ty);

                var document = new Document(pdfDoc);
                document.add(image);
                document.close();

                if (tmpFile != null) {
                    tmpFile.delete();
                }
                pdfDoc.close();
                return newFilePath;
            }

        } catch (IOException e) {
            log.error(String.format("can't load pdf at \"%s\" or can't load image at \"%s\"", filePath, imagePath), e);
        } catch (Exception e) {
            log.error(String.format("can't draw image \"%s\" on pdf doc \"%s\"", imagePath, filePath), e);
        }

        return null;
    }

    public ResponseEntity<RecipientDto> approval(int recipientId, Collection<FieldUpdateRequest> fieldUpdateRequestCollection, ProcessApprovalDto processApprovalDto) {
        log.info("approval recipient: {}", recipientId);
        Date processAt = null;
        if (processApprovalDto.getProcessAt() != null) {
            try {
                processAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .parse(processApprovalDto.getProcessAt());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        var recipientOptional = recipientService.approval(
                recipientId, fieldUpdateRequestCollection, processAt
        );

        if (recipientOptional.isPresent()) {
            fieldUpdateRequestCollection.forEach(fieldUpdateRequest -> process(fieldUpdateRequest.getId(), processApprovalDto.getSignInfo())
            );

            return ResponseEntity.ok(recipientOptional.get());
        }

        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> approvalSignImage(CustomerUser currentCustomer, int recipientId, ProcessApprovalDto processApprovalDto) {
        int otp = processApprovalDto.getOtp();
        var message = verifyOtp(currentCustomer, recipientId, otp);
        if (message.isSuccess() == false) {
            return ResponseEntity.ok().body(message);
        }

        return approval(recipientId, processApprovalDto.getFields(), processApprovalDto);
    }

    public PcksMergeResponse mergeTimestamp(PkcsMergeRequest pkcsMergeRequest) {
        var restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("TenantCode", "mobifone.vn");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("signatureToken", pkcsMergeRequest.getSignature());
        body.add("hexDigestTempFile", pkcsMergeRequest.getHexDigestTempFile());
        body.add("fieldName", pkcsMergeRequest.getFiledName());
        body.add("base64Cert", pkcsMergeRequest.getCert());
        body.add("isTimestamp", pkcsMergeRequest.getIsTimestamp());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        var response = restTemplate.postForObject(pkcsMergeUrl, requestEntity, PcksMergeResponse.class);
        String hex = response.getHexDataSigned();

        if (StringUtils.hasText(hex)) {
            response.setHexDataSigned(null); // decrease size
            response.setBase64Data(Base64Utils.encodeToString(ByteUtil.decodeHexString(hex)));
        }

        return response;
    }
        
    public OtpMessageDto genOtp(int recipientId, GenOtpRequest otpRequest) {
        log.info("==> START Call API.");
        try {
            log.info("==> Lấy thông tin người xử lý.");

            var recipient = recipientRepository.findById(recipientId).orElse(null);
            if (recipient == null) {
                return OtpMessageDto
                        .builder()
                        .success(false)
                        .message("Cannot find recipient")
                        .build();
            }

            log.info("==> Lấy thông tin số lần nhập OTP.");

            int attempts = recipient.getAttempts();

            if (attempts >= 5) {
                return OtpMessageDto
                        .builder()
                        .success(false)
                        .message("You have entered wrong otp 5 times in a row")
                        .nextAttempt(recipient.getNextAttempt())
                        .build();
            }

            log.info("==> Bắt đầu generate OTP.");

            int otp = otpService.generateOTP(recipientId);

            log.info("==> Generate OTP success.");

            log.info("==> Lấy thông tin hợp đồng.");
            int contractId = otpRequest.getContractId();
            var contract = contractRepository.findById(contractId).orElse(null);

            if (contract == null) {
                return OtpMessageDto
                        .builder()
                        .success(false)
                        .message("Cannot find contract")
                        .build();
            }

            String message = String.format(otpTemplate, otp);
            message = VNCharacterUtils.removeAccent(message);

            SmsLogRequest smsLogRequest = SmsLogRequest.builder()
            		.isdn(otpRequest.getPhone())
                    .orgId(contract.getOrganizationId())
            		.mtcontent(message)
                    .contractId(contract.getId())
            		.build();

            Optional<MessageDto> messageOptinal = notificationService.notificationOTP(smsLogRequest);

            log.info("Call API send SMS success: {}",messageOptinal);
            if(messageOptinal.isPresent()) {
            	return OtpMessageDto.builder()
                        .success(true)
                        .build();
            }

        } catch (Exception ex) {
            log.error("send otp error", ex);
        }

        return OtpMessageDto.builder()
                .success(false)
                .message("Can't generate OTP code")
                .build();
    }

    @Transactional
    private OtpMessageDto verifyOtp(CustomerUser currentCustomer, int recipientId, int otp) {
        var recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return OtpMessageDto.builder()
                    .success(false)
                    .message("Cannot find recipient")
                    .build();
        }

        if (currentCustomer.getEmail().equals(recipient.getEmail())) {
            int serverOtp = otpService.getOtp(recipientId);

            if (serverOtp == 0) {
                return OtpMessageDto.builder()
                        .success(false)
                        .message("Otp code has been expired")
                        .build();
            }

            if (otp == serverOtp) {
                return OtpMessageDto.builder()
                        .success(true)
                        .build();
            }

            if (recipient.getAttempts() < 5) {
                recipient.setAttempts(recipient.getAttempts() + 1);
            }
            LocalDateTime nextAttempt = null;
            String message = "Wrong otp";

            if (recipient.getAttempts() >= 5) {
                nextAttempt = LocalDateTime.now().plusMinutes(30).plusSeconds(5);
                if (recipient.getNextAttempt() == null) {
                    recipient.setNextAttempt(nextAttempt);
                }
                message = "You have entered wrong otp 5 times in a row";

            }

            recipientRepository.save(recipient);
            return OtpMessageDto
                    .builder()
                    .success(false)
                    .message(message)
                    .nextAttempt(recipient.getNextAttempt())
                    .build();
        }

        return OtpMessageDto.builder()
                .success(false)
                .message("You cannot perform this action")
                .build();
    }

    public OtpMessageDto checkRecipientStatus(int recipientId) {
        var recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return OtpMessageDto.builder()
                    .success(false)
                    .message("Cannot find recipient")
                    .build();
        }

        if (recipient.getAttempts() >= 5) {
            if (recipient.getNextAttempt().isAfter(LocalDateTime.now())) {
                return OtpMessageDto
                        .builder()
                        .success(false)
                        .locked(true)
                        .message("You have entered wrong otp 5 times in a row")
                        .nextAttempt(recipient.getNextAttempt())
                        .build();
            }

            // reset status lock
            recipient.setAttempts(0);
            recipient.setNextAttempt(null);
            recipientRepository.save(recipient);
        }

        return OtpMessageDto
                .builder()
                .locked(false)
                .build();
    }

    public MessageDto resendSmsEmail(int recipientId) {
        var recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return MessageDto.builder()
                    .success(false)
                    .message("Can't find recipient")
                    .build();
        }

        var participant = recipient.getParticipant();
        var contract = participant.getContract();
        var org = customerService.getOrganizationByCustomer(contract.getCreatedBy()).get();

        if ("phone".equals(recipient.getLoginBy())) {
            String path = org.getPath();
            if (path.contains(".")) {
                path = path.substring(0, path.indexOf('.'));
            }
            var rootOrg = customerService.getOrganizationById(Integer.valueOf(path)).get();
            if (rootOrg.getNumberOfSms() <= 0) {
                return MessageDto.builder()
                        .success(false)
                        .message("Tổ chức đã sử dụng hết số lượng SMS đã mua. Liên hệ với Admin để tiếp tục sử dụng dịch vụ")
                        .build();
            }
        }

        var customer = customerService.getCustomerById(contract.getCreatedBy());

        var request = new SignFlowNotifyRequest();

        request.setActionType(recipient.getRole().getDbVal());
        int approveType = contract.getStatus() == ContractStatus.SIGNED ? 3 : ContractApproveType.APPROVAL.getDbVal();
        request.setApproveType(approveType);

        request.setContractId(contract.getId());
        request.setContractName(contract.getName());
        request.setContractUrl("" + contract.getId());
        request.setContractNotes(contract.getNotes());

        request.setAccessCode(recipient.getPassword());

        if (recipient.getPassword() != null && !recipient.getPassword().equals("")) {
            request.setLoginType("1");
        }

        request.setParticipantName(participant.getName());

        request.setRecipientId("" + recipient.getId());
        request.setRecipientName(recipient.getName());
        request.setRecipientEmail(recipient.getEmail());
        request.setRecipientPhone(recipient.getPhone());

        request.setSenderName(customer.getName());
        request.setSenderParticipant(org.getName());
        request.setLoginBy(recipient.getLoginBy());
        request.setOrgId(contract.getOrganizationId());
        request.setBrandName(org.getBrandName());
        request.setSmsUser(org.getSmsUser());
        request.setSmsPass(org.getSmsPass());
        request.setSmsSendMethor(org.getSmsSendMethor());
        request.setContractUid(contract.getContractUid());

        notificationService.sendSignFlowNotify(request);
        return MessageDto.builder()
                .success(true)
                .build();
    }
}
