package com.vhc.ec.contract.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.definition.ParticipantType;
import com.vhc.ec.contract.definition.SignType;
import com.vhc.ec.contract.dto.ContractDto;
import com.vhc.ec.contract.dto.CopyFileRequest;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.SignTypeDto;
import com.vhc.ec.contract.dto.TemplateDocumentDto;
import com.vhc.ec.contract.dto.TemplateRecipientDto;
import com.vhc.ec.contract.dto.ValidateDto;
import com.vhc.ec.contract.dto.WorkflowDto;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Document;
import com.vhc.ec.contract.entity.Field;
import com.vhc.ec.contract.entity.Participant;
import com.vhc.ec.contract.entity.Recipient;
import com.vhc.ec.contract.entity.TemplateDocument;
import com.vhc.ec.contract.entity.TemplateField;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.repository.FieldRepository;
import com.vhc.ec.contract.repository.ParticipantRepository;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.repository.TemplateContractRepository;
import com.vhc.ec.contract.repository.TemplateDocumentRepository;
import com.vhc.ec.contract.repository.TemplateFieldRepository;
import com.vhc.ec.contract.repository.TemplateParticipantRepository;
import com.vhc.ec.contract.util.ExcelUtil;
import com.vhc.ec.contract.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Process batch service
 *
 * @author VHC, JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {
    private static final String DATE_FORMAT = "yyyy/MM/dd";
    private static final String DATE_COLUMN_FORMAT = "dd/MM/yyyy";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PHONE_REGEX = "[0-9]{10}|[0-9]{11}";
    private static final String CARID_REGEX = "[0-9]{9}|[0-9]{12}";
    private static final String MST_REGEX = "[0-9-]{10}|[0-9-]{14}";
    private static final SimpleDateFormat dff = new SimpleDateFormat(DATE_COLUMN_FORMAT);

    private final TemplateContractRepository templateContractRepository;
    private final TemplateDocumentRepository templateDocumentRepository;
    private final TemplateParticipantRepository templateParticipantRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final ContractRepository contractRepository;
    private final DocumentRepository documentRepository;
    private final ParticipantRepository participantRepository;
    private final RecipientRepository recipientRepository;
    private final FieldRepository fieldRepository;
    private final CustomerService customerService;
    private final FileService fileService;
    private final BpmService bpmService;
    private final ModelMapper modelMapper;
    private final ProcessService processService;
    private final ContractService contractService;

    /**
     * Generate Excel file for contract
     *
     * @param id contract template id
     * @return {@link TemplateDocumentDto}
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     */
    @SuppressWarnings("resource")
	@Transactional
    public Optional<TemplateDocumentDto> generateExcel(int id) { 
        // MARK: get template of contract
        var contractOptional = templateContractRepository.findById(id);

        var templateFieldCodeOptional = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(
                id
        ).stream().filter(val -> val.getType() == FieldType.CONTRACT_NO).findFirst();
        var fieldCodeRequired = false;
        if (templateFieldCodeOptional.isPresent()) {
            var templateFieldCode = templateFieldCodeOptional.get();
            if (templateFieldCode.getRecipientId() == null || templateFieldCode.getRecipientId() == 0) {
                fieldCodeRequired = true;
            }
        }
        if (contractOptional.isPresent()) {
            // initials excel file
            var workbook = new XSSFWorkbook();
            var sheet = workbook.createSheet();

            var colIndex = 0;
            var headerRow = sheet.createRow(0);
            
            //cellStyle String
            final var fmt = workbook.createDataFormat();
            var cellStyle_string = workbook.createCellStyle();
            cellStyle_string.setDataFormat(fmt.getFormat("@"));
            
            //cellStyle Date
            var cellStyle_date = workbook.createCellStyle();
            cellStyle_date.setDataFormat(fmt.getFormat(DATE_COLUMN_FORMAT));
            
            //cellStyle WrapText
            var cellStyle_wrapText = workbook.createCellStyle();
            cellStyle_wrapText.setWrapText(true);

            // add contract fields
            var contractFields = new String[]{
                    "Tên hợp đồng (*)",
                    fieldCodeRequired ? "Số hợp đồng (*)" : "Số hợp đồng",
                    "Ngày hết hạn ký (*)"
            };
            for (var field : contractFields) {
            	createCellHeader(headerRow, colIndex, cellStyle_wrapText, field);
            	
                colIndex = colIndex + 1;
            }

            var contract = contractOptional.get();

            // MARK: fields
            var templateFieldCollection = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(contract.getId());
            if (templateFieldCollection != null && templateFieldCollection.size() > 0) {
                for (var templateField : templateFieldCollection) {
                    if ((templateField.getRecipientId() == null || templateField.getRecipientId() == 0) && templateField.getType() == FieldType.TEXT) {
                        createCellHeader(headerRow, colIndex, cellStyle_wrapText, templateField.getName() + " (*) ");
                        colIndex = colIndex + 1;
                    }
                }
            }

            // Create header Tổ chức của tôi
            var myParticipantOptional = templateParticipantRepository.findMyParticipantByContractId(contract.getId());
            if(myParticipantOptional.isPresent()) {
            	var myParticipant = myParticipantOptional.get();
            	var myRecipientCollection = myParticipant.getRecipients();
            	
            	if (myRecipientCollection != null && myRecipientCollection.size() > 0) {
            		var coordinatorIndex = 1; 
            		var reviewerIndex = 1; 
            		var signerIndex = 1; 
            		var archiverIndex = 1;
                    for (var recipient : myRecipientCollection) {
                        // add recipient
                        var recipientRole = "";
                        switch (recipient.getRole()) {
                            case COORDINATOR:
                                recipientRole = "người điều phối "+ coordinatorIndex;
                                coordinatorIndex++;
                                break;
                            case REVIEWER:
                                recipientRole = "người xem xét "+reviewerIndex;
                                reviewerIndex++;
                                break;
                            case SIGNER:
                                recipientRole = "người ký "+signerIndex;
                                signerIndex++;
                                break;
                            case ARCHIVER:
                                recipientRole = "văn thư "+archiverIndex;
                                archiverIndex++;
                                break;
                            default:
                                break;
                        }

                        if (StringUtils.hasText(recipientRole)) {
                            var participantName = "Tổ chức của tôi";

                            // recipient name 
                            createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Tên %s (*)", participantName, recipientRole));
                            colIndex = colIndex + 1; 
                             
                            // recipient email 
                            if(StringUtils.hasText(recipient.getLoginBy())) {
                            	 if(recipient.getLoginBy().equals("phone")) {
                            		//set default column format String
                                    sheet.setDefaultColumnStyle(colIndex, cellStyle_string);
                                    
                                    createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Sđt %s (*)", participantName, recipientRole));
                        			colIndex = colIndex + 1;
                            	 }else {
                            		 createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Email %s (*)", participantName, recipientRole));
                                     colIndex = colIndex + 1;
                            	 }
                            } 
                            
                            /**
                             * 	bổ sung phone và cccd
                             * 	recipient phone chỉ có khi đăng nhập bằng số điện thoại hoặc ký ảnh OTP
                             *  recipient cccd chỉ có và bắt buộc nhập nếu là ký eKYC
                             *  
                             */
                            TemplateRecipientDto recipientDto = modelMapper.map(
                                    recipient, new TypeToken<TemplateRecipientDto>() {
                                    }.getType());
                            
                            final var signTypeList = recipientDto.getSignType(); 
                            
                            if (signTypeList != null && signTypeList.size() > 0) {
                            	for(SignTypeDto signType : signTypeList) {
                            		//set default column format String
                                    sheet.setDefaultColumnStyle(colIndex, cellStyle_string);
                                    
                            		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) { 
                            			createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Sđt %s (*)", participantName, recipientRole));
                            			colIndex = colIndex + 1;
                            		}
                            		
                            		if(signType.getId() == SignType.USB_TOKEN.getDbVal()) {
    			                    	//recipient cccd 
                            			createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. MST/CMT/CCCD %s (*)", participantName, recipientRole));
        			                    colIndex = colIndex + 1;
    			                    }else if(signType.getId() == SignType.EKYC.getDbVal()) {
    			                    	//recipient eKYC 
    			                    	createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. CMT/CCCD %s (*)", participantName, recipientRole));
        			                    colIndex = colIndex + 1;
    			                    }else if(signType.getId() == SignType.HSM.getDbVal()) {
    			                    	//recipient eKYC
                                    	createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. MST %s (*)", participantName, recipientRole));
        			                    colIndex = colIndex + 1;
    			                    }
                            	}
                            } 
                        } 
                    }
                }
            }
            
            // Create header đối tác
            var partnerParticipantCollection = templateParticipantRepository.findPartnerByContractIdOrderById(contract.getId());
            if (partnerParticipantCollection != null && partnerParticipantCollection.size() > 0) {
                var participantIndex = 1;
                for (var participant : partnerParticipantCollection) {
                	//Bổ sung cột Tên tổ chức nếu đối tác là Tổ chức
                	System.out.println("participant.getType() = "+participant.getType());
                	System.out.println("ParticipantType.ORGANIZATION = "+ParticipantType.ORGANIZATION);
                	if (participant.getType() == ParticipantType.ORGANIZATION) {
                		// add participant name 
                        createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("Tên đối tác %d (*)", participantIndex));
                        colIndex = colIndex + 1;
                	} 

                    // add recipients of participant
                    var recipientCollection = participant.getRecipients();
                    if (recipientCollection != null && recipientCollection.size() > 0) {
                    	var coordinatorIndex = 1; 
                		var reviewerIndex = 1; 
                		var signerIndex = 1; 
                		var archiverIndex = 1;
                        for (var recipient : recipientCollection) { 
                            // add recipient
                            var recipientRole = "";
                            switch (recipient.getRole()) {
	                            case COORDINATOR:
	                                recipientRole = "người điều phối "+ coordinatorIndex;
	                                coordinatorIndex++;
	                                break;
	                            case REVIEWER:
	                                recipientRole = "người xem xét "+reviewerIndex;
	                                reviewerIndex++;
	                                break;
	                            case SIGNER:
	                                recipientRole = "người ký "+signerIndex;
	                                signerIndex++;
	                                break;
	                            case ARCHIVER:
	                                recipientRole = "văn thư "+archiverIndex;
	                                archiverIndex++;
	                                break;
	                            default:
	                                break;
	                        }

                            if (StringUtils.hasText(recipientRole)) {
                                var participantName = String.format("Đối tác %d", participantIndex);

                                // recipient name 
                                createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Tên %s (*)", participantName, recipientRole));
                                colIndex = colIndex + 1;

                                // recipient email 
                                if(StringUtils.hasText(recipient.getLoginBy())) {
                                	 if(recipient.getLoginBy().equals("phone")) {
                                		//set default column format String
                                        sheet.setDefaultColumnStyle(colIndex, cellStyle_string);
                                        
                                        createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Sđt %s (*)", participantName, recipientRole));
                            			colIndex = colIndex + 1;
                                	 }else {
                                		 createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Email %s (*)", participantName, recipientRole));
                                         colIndex = colIndex + 1;
                                	 }
                                }
                                
                                /**
                                 * 	bổ sung phone và cccd
                                 * 	recipient phone luôn có và bắt buộc nhập nếu là ký ảnh OTP
                                 *  recipient cccd chỉ có và bắt buộc nhập nếu là ký eKYC
                                 *  
                                 */
                                TemplateRecipientDto recipientDto = modelMapper.map(
                                        recipient, new TypeToken<TemplateRecipientDto>() {
                                        }.getType());
                                
                                final var signTypeList = recipientDto.getSignType(); 
                                
                                if (signTypeList != null && signTypeList.size() > 0) {
                                	for(SignTypeDto signType : signTypeList) { 
                                		//set default column format String 
                                        sheet.setDefaultColumnStyle(colIndex, cellStyle_string);
                                        
                                        if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) { 
                                			createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. Sđt %s (*)", participantName, recipientRole));
                                			colIndex = colIndex + 1;
                                		}
                                        
                                        if(signType.getId() == SignType.USB_TOKEN.getDbVal()) {
        			                    	//recipient cccd 
                                        	createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. MST/CMT/CCCD %s (*)", participantName, recipientRole));
            			                    colIndex = colIndex + 1;
        			                    }else if(signType.getId() == SignType.EKYC.getDbVal()) {
        			                    	//recipient eKYC 
                                        	createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. CMT/CCCD %s (*)", participantName, recipientRole));
            			                    colIndex = colIndex + 1;
        			                    }else if(signType.getId() == SignType.HSM.getDbVal()) {
        			                    	//recipient eKYC 
                                        	createCellHeader(headerRow, colIndex, cellStyle_wrapText, String.format("%s. MST %s (*)", participantName, recipientRole));
            			                    colIndex = colIndex + 1;
        			                    }
                                	}
                                }
                            }
                        }
                    }

                    participantIndex = participantIndex + 1;
                }
            }
            
            
            var sampleRow = sheet.createRow(1);

            // insert sample data
            sampleRow.createCell(0, CellType.STRING).setCellValue(contract.getName());
            sampleRow.createCell(1, CellType.STRING).setCellValue("");

            // end time column
            var cell = sampleRow.createCell(2); 
            
            // mặc định ngày hết hạn hợp đồng
            cell.setCellValue(com.vhc.ec.contract.util.DateUtil.addDate(new Date(), 30));
            cell.setCellStyle(cellStyle_date);
            //set default column format date
            sheet.setDefaultColumnStyle(2, cellStyle_date); 

            colIndex = 3;

            // set default value of fields
            if (templateFieldCollection != null && templateFieldCollection.size() > 0) {
                for (var templateField : templateFieldCollection) {
                    if ((templateField.getRecipientId() == null || templateField.getRecipientId() == 0) && templateField.getType() == FieldType.TEXT) {
                        sampleRow.createCell(colIndex, CellType.STRING).setCellValue(
                                templateField.getValue()
                        );
                        colIndex = colIndex + 1;
                    }
                }
            }
            
            // Dữ liệu Tổ chức của tôi
            if (myParticipantOptional.isPresent()) {
                var myParticipant = myParticipantOptional.get();
                
                // add recipients of participant
                var recipientCollection = myParticipant.getRecipients();
                if (recipientCollection != null && recipientCollection.size() > 0) {
                    for (var recipient : recipientCollection) {
                        // recipient name
                        sampleRow.createCell(colIndex, CellType.STRING).setCellValue(recipient.getName());
                        colIndex = colIndex + 1;

                        // recipient email 
                        if(StringUtils.hasText(recipient.getLoginBy())) {
                        	 if(recipient.getLoginBy().equals("phone")) {
                                 var cell_phone = sampleRow.createCell(colIndex);
                                 cell_phone.setCellStyle(cellStyle_string);
                                 cell_phone.setCellValue(recipient.getPhone());

                    			 colIndex = colIndex + 1;
                        	 }else {
                        		 sampleRow.createCell(colIndex, CellType.STRING).setCellValue(recipient.getEmail());
                                 colIndex = colIndex + 1;
                        	 }
                        } 
                        
                        // recipient cccd, phone
                        TemplateRecipientDto recipientDto = modelMapper.map(
                                recipient, new TypeToken<TemplateRecipientDto>() {
                                }.getType());
                        
                        final var signTypeList = recipientDto.getSignType();
                        
                        if (signTypeList != null && signTypeList.size() > 0) {
                        	for(SignTypeDto signType : signTypeList) { 
                        		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) { 
                        			//recipient phone  
                                    //set default cell format String
                        			var cell_phone = sampleRow.createCell(colIndex);
                                    cell_phone.setCellStyle(cellStyle_string);
                                    cell_phone.setCellValue(recipient.getPhone());
                                    
            	                    colIndex = colIndex + 1;
                        		}
                        		
                        		if(signType.getId() == SignType.EKYC.getDbVal()
                        				|| signType.getId() == SignType.USB_TOKEN.getDbVal() 
                        				|| signType.getId() == SignType.HSM.getDbVal()
                        			) {
			                    	//recipient cccd
                        			//set default cell format String
                                    var cell_cccd = sampleRow.createCell(colIndex);
                                    cell_cccd.setCellStyle(cellStyle_string);
                                    cell_cccd.setCellValue(recipient.getCardId());
                                    
    			                    colIndex = colIndex + 1;
			                    }
                        	}
                        }  
                    }
                }
            }
            
            // Dữ liệu đối tác
            if (partnerParticipantCollection != null && partnerParticipantCollection.size() > 0) {
                for (var participant : partnerParticipantCollection) {
                    if (participant.getType() == ParticipantType.ORGANIZATION) {
                    	// add participant name
                        sampleRow.createCell(colIndex, CellType.STRING).setCellValue(participant.getName());
                        colIndex = colIndex + 1;
                    }

                    // add recipients of participant
                    var recipientCollection = participant.getRecipients();
                    if (recipientCollection != null && recipientCollection.size() > 0) {
                        for (var recipient : recipientCollection) {
                            // recipient name
                            sampleRow.createCell(colIndex, CellType.STRING)
                                    .setCellValue(recipient.getName());
                            colIndex = colIndex + 1;

                            // recipient email 
                            if(StringUtils.hasText(recipient.getLoginBy())) {
                            	 if(recipient.getLoginBy().equals("phone")) {
                                     var cell_phone = sampleRow.createCell(colIndex);
                                     cell_phone.setCellStyle(cellStyle_string);
                                     cell_phone.setCellValue(recipient.getPhone());

                                     colIndex = colIndex + 1;
                            	 }else {
                            		 sampleRow.createCell(colIndex, CellType.STRING).setCellValue(recipient.getEmail());
                                     colIndex = colIndex + 1;
                            	 }
                            } 
                            
                            // recipient cccd, phone
                            TemplateRecipientDto recipientDto = modelMapper.map(
                                    recipient, new TypeToken<TemplateRecipientDto>() {
                                    }.getType());
                            
                            final var signTypeList = recipientDto.getSignType();
                            
                            if (signTypeList != null && signTypeList.size() > 0) {
                            	for(SignTypeDto signType : signTypeList) { 
                            		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) { 
                            			//recipient phone
                            			var cell_phone = sampleRow.createCell(colIndex);
                                        cell_phone.setCellStyle(cellStyle_string);
                                        cell_phone.setCellValue(recipient.getPhone());
                                        
                	                    colIndex = colIndex + 1;
                            		}
                            		
                            		if(signType.getId() == SignType.EKYC.getDbVal() 
                            				|| signType.getId() == SignType.USB_TOKEN.getDbVal() 
                            				|| signType.getId() == SignType.HSM.getDbVal()
                            			) {
    			                    	//recipient cccd
                            			var cell_cccd = sampleRow.createCell(colIndex);
                                        cell_cccd.setCellStyle(cellStyle_string);
                                        cell_cccd.setCellValue(recipient.getCardId());
                                        
        			                    colIndex = colIndex + 1;
    			                    }
                            	}
                            }
                        }
                    } 
                }
            }
            
            //var temporaryFilePath = String.format("D:\\workspace2\\econtract-service\\application\\ec-contract-srv/tmp/%s.xlsx", UUID.randomUUID());
            var temporaryFilePath = String.format("/tmp/%s.xlsx", UUID.randomUUID());
            try (var out = new FileOutputStream(temporaryFilePath)) {
                workbook.write(out); 
                
                // upload temporary file to MinIO
                var documentOptional = templateDocumentRepository
                        .findAllByContractIdAndStatusOrderByTypeDesc(id, BaseStatus.ACTIVE.ordinal())
                        .stream().filter(val -> val.getType() == DocumentType.PRIMARY)
                        .findFirst();
                if (documentOptional.isPresent()) {
                    var newDocument = new TemplateDocument();
                    BeanUtils.copyProperties(
                            documentOptional.get(),
                            newDocument,
                            "id", "type", "path", "filename"
                    );

                    var sdf = new SimpleDateFormat(DATE_FORMAT);
                    var newFileName = String.format("%s.xlsx", UUID.randomUUID());
                    var newPath = String.format("%s/%s", sdf.format(new Date()), newFileName);

                    var uploadOptional = fileService.replace(temporaryFilePath, newDocument.getBucket(), newPath);
                    if (uploadOptional.isPresent()) {
                    	final var presignedUrl = fileService.getPresignedObjectUrl(
                    			newDocument.getBucket(),
                    			newPath
                        );
                    	
                        newDocument.setType(DocumentType.BATCH);
                        newDocument.setFilename(String.format("%s.xlsx", contract.getName()));
                        newDocument.setPath(presignedUrl);

                        newDocument = templateDocumentRepository.save(newDocument);
                        return Optional.of(
                                modelMapper.map(newDocument, TemplateDocumentDto.class)
                        );
                    }
                }
            } catch (IOException e) {
                log.error("can't open temporary file {}", temporaryFilePath);
            }
        }

        return Optional.empty();
    }

    public ResponseEntity<ValidateDto> validate(int templateId, MultipartFile multipartFile, int organizationId, boolean deleteTempFile) {
        // lưu tệp tin người dùng tải lên vào thư mục tạm
        var temporaryFilePath = String.format("/tmp/%s.%s", UUID.randomUUID(), FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        //var temporaryFilePath = String.format("D:\\workspace2\\econtract-service\\application\\ec-contract-srv/%s.%s", UUID.randomUUID(), FilenameUtils.getExtension(multipartFile.getOriginalFilename()));

        try {
            multipartFile.transferTo(new File(temporaryFilePath));

            var validateDto = validation(
                    templateId, temporaryFilePath, organizationId
            );

            validateDto.setTempFile(temporaryFilePath);
            return ResponseEntity.ok(validateDto);
        } catch (IOException e) {
            log.error("can't write customer upload file \"{}\"", temporaryFilePath, e);
        } finally {
            try {
                if (deleteTempFile) {
                    FileUtils.delete(new File(temporaryFilePath));
                }

            } catch (IOException e) {
                log.error("can't delete temporary file \"{}\"", temporaryFilePath, e);
            }
        }

        return ResponseEntity.badRequest()
                .build();
    }
    /***
     * validation template uploaded file
     *
     * @param id uuid of template contract
     * @param temporaryFilePath temporary template uploaded file
     * @return {@link ValidateDto}
     */
    @SuppressWarnings("unused")
	private ValidateDto validation(int id, String temporaryFilePath, int organizationId) {
        var validate = ValidateDto.builder()
                .success(false)
                .message("internal error")
                .detail(null)
                .build();

        var contractOptional = templateContractRepository.findById(id);
        var templateFieldCodeOptional = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(id)
        		.stream().filter(val -> val.getType() == FieldType.CONTRACT_NO).findFirst();
        
        //Kiểm tra số hợp đồng có được gán người xử lý hay không
        var fieldCodeRequired = false;
        if (templateFieldCodeOptional.isPresent()) {
            var templateFieldCode = templateFieldCodeOptional.get();
            if (templateFieldCode.getRecipientId() == null || templateFieldCode.getRecipientId() == 0) {
                fieldCodeRequired = true;
            }
        }
        
        //Kiểm tra email không được trùng nhau
        HashMap<String, String> emailMap = new HashMap<String, String>();
        
        //Kiểm tra tên đối tác không được trùng nhau
        HashMap<String, String> participantMap = new HashMap<String, String>();
        
        //Kiểm tra số hợp đồng không được trùng nhau
        HashMap<String, String> contractNoDuplicateMap = new HashMap<String, String>();
        
        //Kiểm tra số hợp đồng đã tồn tại trong hệ thống
        HashMap<String, String> contractNoMap = new HashMap<String, String>();
        Collection<Contract> contractCollection = contractRepository.findByOrganizationIdAndStatusNot(organizationId, ContractStatus.DRAFF);
        contractCollection = contractCollection.stream().filter(
        		contract -> (contract.getContractNo() != null)
        ).collect(Collectors.toList());
        for(Contract contract : contractCollection) {
        	contractNoMap.putIfAbsent(contract.getContractNo().toUpperCase(), contract.getContractNo());
        }
        
        //Kiểm tra phone không được trùng nhau
        HashMap<String, String> phoneMap = new HashMap<String, String>();
        
        //Kiểm tra cccd không được trùng nhau
        HashMap<String, String> cccdMap = new HashMap<String, String>();
        
        //Validate file import
        if (contractOptional.isPresent()) {
            List<String> validatedMessageList = new ArrayList<>();

            try {
                // parse temporary template file
                var workbook = WorkbookFactory.create(new File(temporaryFilePath));
                var sheet = workbook.getSheetAt(0);

                var contract = contractOptional.get();
                var totalColumns = 3;

                // count total participants
                totalColumns = totalColumns +
                        (int) contract.getParticipants()
                                .stream().filter(participant -> participant.getType() == ParticipantType.ORGANIZATION)
                                .count();

                // count total fields
                var templateFieldCollection = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(contract.getId());
                if (templateFieldCollection != null && templateFieldCollection.size() > 0) {
                    templateFieldCollection = templateFieldCollection.stream().filter(
                            field -> (field.getRecipientId() == null || field.getRecipientId() == 0) && field.getType() == FieldType.TEXT
                    ).collect(Collectors.toList());

                    totalColumns = totalColumns + templateFieldCollection.size();
                }

                // count total recipients (multiple with 2 because contain name & email address/phone)
                totalColumns = totalColumns + contract.getParticipants().stream()
                        .map(participant -> participant.getRecipients().size())
                        .mapToInt(value -> value).sum() * 2;
                
                // count total cccd theo loai ky eKYC
                var templateParticipantCollection = templateParticipantRepository.findByContractIdOrderByOrderingAsc(contractOptional.get().getId());
                if(templateParticipantCollection != null && templateParticipantCollection.size() > 0) {
                	for (var participant : templateParticipantCollection) { 
                		var recipientCollection = participant.getRecipients();
                        if (recipientCollection != null && recipientCollection.size() > 0) { 
                        	for (var recipient : recipientCollection) {  
                        		TemplateRecipientDto recipientDto = modelMapper.map(
                                        recipient, new TypeToken<TemplateRecipientDto>() {
                                        }.getType());
                                final var signTypeList = recipientDto.getSignType();
                                
                                if (signTypeList != null && signTypeList.size() > 0) {
                                	for(SignTypeDto signType : signTypeList) {
                                		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) { 
                                			totalColumns = totalColumns + 1;
                                		}
                                		
                                		if(signType.getId() == SignType.EKYC.getDbVal() 
                                				|| signType.getId() == SignType.USB_TOKEN.getDbVal()
                                				|| signType.getId() == SignType.HSM.getDbVal()
                                			) {
                                			totalColumns = totalColumns + 1;
        			                    }
                                	}
                                }
                        	}
                        }
                	}
                } 
                
                if (totalColumns != sheet.getRow(0).getPhysicalNumberOfCells()) {
                    validatedMessageList.add(
                            "Số lượng dữ liệu cột trong tệp tin tải lên không khớp với mẫu hợp đồng bạn đã chọn."
                    );
                }

                var rowIterator = sheet.rowIterator();

                // validation each row
                while (rowIterator.hasNext()) {
                    var row = rowIterator.next();
                    
                    //Kiểm tra dòng empty or null
                    boolean isRowEmpty = true;
                    for(var columnIndex = 0; columnIndex < totalColumns; columnIndex++) {
                    	try {
                    		var cell = row.getCell(columnIndex, MissingCellPolicy.RETURN_NULL_AND_BLANK); 
                    		
                            if (cell != null && StringUtils.hasText(cell.getStringCellValue()))
                            {
                                isRowEmpty = false;
                                break;
                            }
						} catch (Exception e) {
							// TODO: handle exception
						} 
                    }
                    
                    //Validate từ dòng 1 và không phải dòng empty or null
                    if (row.getRowNum() > 0 && !isRowEmpty) {
                        // validation contract name
                        var contractName = ExcelUtil.getCellValue(row.getCell(0)); //row.getCell(0) != null ? row.getCell(0).getStringCellValue() : null;
                        if (!StringUtils.hasText(contractName)) {
                            validatedMessageList.add(
                                    String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, "A")
                            );
                        }

                        // validation contract code
                        var contractNo = ExcelUtil.getCellValue(row.getCell(1));
                        if (!StringUtils.hasText(contractNo)) {
                        	if(fieldCodeRequired) {
                        		validatedMessageList.add(
                                        String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, "B")
                                );
                        	} 
                        }else {
                        	if(contractNoMap.containsKey(contractNo.toUpperCase())) {
                        		validatedMessageList.add(
                                        String.format("Vị trí (%d, %s) Số hợp đồng đã tồn tại trong hệ thống", row.getRowNum() + 1, "B")
                                );
                        	}else{
                        		if(contractNoDuplicateMap.containsKey(contractNo.toUpperCase())) {
                            		validatedMessageList.add(
                                            String.format("Vị trí (%d, %s) Số hợp đồng không được trùng nhau", row.getRowNum() + 1, "B")
                                    );
                            	}else{
                            		contractNoDuplicateMap.putIfAbsent(contractNo.toUpperCase(), contractNo);
                            	};
                        	};
                        }

                        // validation end time
                        var endTime = ExcelUtil.getCellValue(row.getCell(2));
                        if(!StringUtils.hasText(endTime)) {
                			validatedMessageList.add(
                                    String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, "C")
                            );
                		}else {
                			if(!com.vhc.ec.contract.util.DateUtil.isValid(endTime)) {
                				validatedMessageList.add(
                                        String.format("Vị trí (%d, %s) không đúng định dạng thời gian", row.getRowNum() + 1, "C")
                                );
                			}else {
                				Date endDate = dff.parse(endTime);
                    			Date nowDate = dff.parse(dff.format(new Date()));
                    			
                    			if(endDate.before(nowDate)) {
                    				validatedMessageList.add(
                                            String.format("Vị trí (%d, %s) ngày hết hạn ký không được nhỏ hơn ngày hiện tại", row.getRowNum() + 1, "C")
                                    );
                    			}
                			}
                		}

                        var colIndex = 3;
                        
                        // validation field
                        if (templateFieldCollection != null && templateFieldCollection.size() > 0) {
                            for (var templateField : templateFieldCollection) {
                                var fieldValue = ExcelUtil.getCellValue(row.getCell(colIndex));
                                if (!StringUtils.hasText(fieldValue)) {
                                    validatedMessageList.add(
                                            String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                    );
                                }

                                colIndex = colIndex + 1;
                            }
                        } 

                        // validation tổ chức của tôi
                        var myParticipantOptional = templateParticipantRepository.findMyParticipantByContractId(contract.getId());
                        if(myParticipantOptional.isPresent()) {
                        	var myParticipant = myParticipantOptional.get();
                        	var myRecipientCollection = myParticipant.getRecipients(); 
                        	
                        	if (myRecipientCollection != null && myRecipientCollection.size() > 0) {
                        		for (var recipient : myRecipientCollection) {
                                    // validation recipient name
                                    var recipientName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    if (!StringUtils.hasText(recipientName)){
                                        validatedMessageList.add(
                                                String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                        );
                                    }
                                    colIndex = colIndex + 1;

                                    // validation recipient email address/phone
                                    if(StringUtils.hasText(recipient.getLoginBy())) {
                                   	 if(recipient.getLoginBy().equals("phone")) {
                                   		var recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                   		if (!StringUtils.hasText(recipientPhone)){
                                            validatedMessageList.add(
                                                    String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                            );
                                        } else {
                                        	var pattern = Pattern.compile(PHONE_REGEX);
                                            var matcher = pattern.matcher(recipientPhone);

                                            if (!matcher.matches()) {
                                                validatedMessageList.add(
                                                        String.format("Vị trí (%d, %s) không phải là số điện thoại hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                );
                                            }else {
                                            	if(phoneMap.containsKey(recipientPhone)) {
                                            		validatedMessageList.add(
                                                            String.format("Vị trí (%d, %s) Số điện thoại không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                    );
                                            	}else{
                                            		phoneMap.putIfAbsent(recipientPhone, recipientPhone);
                                            	};
                                            }
                                        }
                            			colIndex = colIndex + 1;
                                   	 }else {
                                   		var recipientEmailAddress = ExcelUtil.getCellValue(row.getCell(colIndex)); 
                                        if (!StringUtils.hasText(recipientEmailAddress)) {
                                            validatedMessageList.add(
                                                    String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                            );
                                        } else {
                                            var pattern = Pattern.compile(EMAIL_REGEX);
                                            var matcher = pattern.matcher(recipientEmailAddress);

                                            if (!matcher.matches()) {
                                                validatedMessageList.add(
                                                        String.format("Vị trí (%d, %s) không phải là địa chỉ email hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                );
                                            }else {
                                            	if(emailMap.containsKey(recipientEmailAddress.toUpperCase())) {
                                            		validatedMessageList.add(
                                                            String.format("Vị trí (%d, %s) Email không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                    );
                                            	}else{
                                            		emailMap.putIfAbsent(recipientEmailAddress.toUpperCase(), recipientEmailAddress);
                                            	};
                                            }
                                        } 
                                        
                                        colIndex = colIndex + 1; 
                                   	 }
                                   }  
                            		
                                    // validate recipient CCCD, SĐT
                            		TemplateRecipientDto recipientDto = modelMapper.map(
                            				recipient, new TypeToken<TemplateRecipientDto>() {
                                            }.getType()); 
                                    final var signTypeList = recipientDto.getSignType(); 
                                    if (signTypeList != null && signTypeList.size() > 0) {
                                    	for(SignTypeDto signType : signTypeList) {
                                    		// validate recipient sđt
                                    		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) {
                                    			var recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    			if (!StringUtils.hasText(recipientPhone)){
                                                    validatedMessageList.add(
                                                            String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                    );
                                                } else {
                                                	var pattern = Pattern.compile(PHONE_REGEX);
                                                    var matcher = pattern.matcher(recipientPhone);

                                                    if (!matcher.matches()) {
                                                        validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không phải là số điện thoại hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    }else {
                                                    	if(phoneMap.containsKey(recipientPhone)) {
                                                    		validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) Số điện thoại không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                    	}else{
                                                    		phoneMap.putIfAbsent(recipientPhone, recipientPhone);
                                                    	};
                                                    }
                                                }
                                    			colIndex = colIndex + 1;
                                    		}
                                    		
                                    		// validate recipient cccd
                                    		if(signType.getId() == SignType.EKYC.getDbVal()) {
                                    			var recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    			if (!StringUtils.hasText(recipientCCCD)){
                                                    validatedMessageList.add(
                                                            String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                    );
                                                }else {
                                                	var pattern = Pattern.compile(CARID_REGEX);
                                                    var matcher = pattern.matcher(recipientCCCD);

                                                    if (!matcher.matches()) {
                                                        validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không phải là số CMT/CCCD hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    }else {
                                                    	if(cccdMap.containsKey(recipientCCCD)) {
                                                    		validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) CMT/CCCD không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                    	}else{
                                                    		cccdMap.putIfAbsent(recipientCCCD, recipientCCCD);
                                                    	};
                                                    }
                                                }
                                                colIndex = colIndex + 1;
            			                    }else if(signType.getId() == SignType.USB_TOKEN.getDbVal()) {
            			                    	var recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    			if (!StringUtils.hasText(recipientCCCD)){
                                                    validatedMessageList.add(
                                                            String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                    );
                                                }else {
                                                	var pattern = Pattern.compile(CARID_REGEX);
                                                    var matcher = pattern.matcher(recipientCCCD);

                                                    if (!matcher.matches()) {
                                                    	pattern = Pattern.compile(MST_REGEX);
                                                    	matcher = pattern.matcher(recipientCCCD);
                                                    	
                                                    	if (!matcher.matches()) {
                                                    		validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) không phải là số MST/CMT/CCCD hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                    	}
                                                    }else {
                                                    	if(cccdMap.containsKey(recipientCCCD)) {
                                                    		validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) CMT/CCCD không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                    	}else{
                                                    		cccdMap.putIfAbsent(recipientCCCD, recipientCCCD);
                                                    	};
                                                    }
                                                }
                                                colIndex = colIndex + 1;
            			                    }else if(signType.getId() == SignType.HSM.getDbVal()) {
            			                    	var recipientMST = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    			if (!StringUtils.hasText(recipientMST)){
                                                    validatedMessageList.add(
                                                            String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                    );
                                                }else {
                                                	var pattern = Pattern.compile(MST_REGEX);
                                                    var matcher = pattern.matcher(recipientMST);

                                                    if (!matcher.matches()) {
                                                    	validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không phải là số MST hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    }
                                                }
                                                colIndex = colIndex + 1;
            			                    } 
                                    	}
                                    }
                                }
                        	}
                        } 
                        
                        // validation đối tác
                        var partnerParticipantCollection = templateParticipantRepository.findPartnerByContractIdOrderById(contract.getId());
                        if (partnerParticipantCollection != null && partnerParticipantCollection.size() > 0) {
                        	var participantIndex = 1;
                        	for (var participant : partnerParticipantCollection) {
                        		//validation participant name
                            	if (participant.getType() == ParticipantType.ORGANIZATION) {
                            		var participantName = ExcelUtil.getCellValue(row.getCell(colIndex));

                                    if (!StringUtils.hasText(participantName)) {
                                        validatedMessageList.add(
                                                String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                        );
                                    }else {
                                    	if(participantMap.containsKey(participantName.trim().toUpperCase())) {
                                    		validatedMessageList.add(
                                                    String.format("Vị trí (%d, %s) Tên đối tác không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                            );
                                    	}else{
                                    		participantMap.putIfAbsent(participantName.trim().toUpperCase(), participantName);
                                    	};
                                    }
                                    
                                    colIndex = colIndex + 1;
                            	} 
                            	
                            	var recipientCollection = participant.getRecipients();
                                if (recipientCollection != null && recipientCollection.size() > 0) { 
                                	for (var recipient : recipientCollection) { 
                                        // validation recipient name
                                        var recipientName = ExcelUtil.getCellValue(row.getCell(colIndex)); 
                                        if (!StringUtils.hasText(recipientName)) {
                                            validatedMessageList.add(
                                                    String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                            );
                                        }
                                        colIndex = colIndex + 1;

                                        // validation recipient email address
                                        if(StringUtils.hasText(recipient.getLoginBy())) {
                                          	 if(recipient.getLoginBy().equals("phone")) {
                                          		var recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
	                                   			if (!StringUtils.hasText(recipientPhone)){
	                                                   validatedMessageList.add(
	                                                           String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
	                                                   );
	                                               } else {
	                                            	   var pattern = Pattern.compile(PHONE_REGEX);
	                                                   var matcher = pattern.matcher(recipientPhone);
	
	                                                   if (!matcher.matches()) {
	                                                       validatedMessageList.add(
	                                                               String.format("Vị trí (%d, %s) không phải là số điện thoại hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
	                                                       );
	                                                   }else {
	                                                   	if(phoneMap.containsKey(recipientPhone)) {
	                                                   		validatedMessageList.add(
	                                                                   String.format("Vị trí (%d, %s) Số điện thoại không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
	                                                           );
	                                                   	}else{
	                                                   		phoneMap.putIfAbsent(recipientPhone, recipientPhone);
	                                                   	};
	                                                   }
	                                               }
	                                   			colIndex = colIndex + 1;
                                          	 }else {
                                          		var recipientEmailAddress = ExcelUtil.getCellValue(row.getCell(colIndex)); 
                                               if (!StringUtils.hasText(recipientEmailAddress)) {
                                                   validatedMessageList.add(
                                                           String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                   );
                                               } else {
                                                   var pattern = Pattern.compile(EMAIL_REGEX);
                                                   var matcher = pattern.matcher(recipientEmailAddress);

                                                   if (!matcher.matches()) {
                                                       validatedMessageList.add(
                                                               String.format("Vị trí (%d, %s) không phải là địa chỉ email hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                       );
                                                   }else {
                                                   	if(emailMap.containsKey(recipientEmailAddress.toUpperCase())) {
                                                   		validatedMessageList.add(
                                                                   String.format("Vị trí (%d, %s) Email không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                           );
                                                   	}else{
                                                   		emailMap.putIfAbsent(recipientEmailAddress.toUpperCase(), recipientEmailAddress);
                                                   	};
                                                   }
                                               } 
                                               
                                               colIndex = colIndex + 1; 
                                          	 }
                                          }
                                        
                                        // validate recipient CCCD, SĐT
                                		TemplateRecipientDto recipientDto = modelMapper.map(
                                				recipient, new TypeToken<TemplateRecipientDto>() {
                                                }.getType()); 
                                        final var signTypeList = recipientDto.getSignType(); 
                                        if (signTypeList != null && signTypeList.size() > 0) {
                                        	for(SignTypeDto signType : signTypeList) {
                                        		// validate recipient sđt
                                        		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) { 
                                        			var recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        			if (!StringUtils.hasText(recipientPhone)){
                                                        validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    } else {
                                                    	var pattern = Pattern.compile(PHONE_REGEX);
                                                        var matcher = pattern.matcher(recipientPhone);

                                                        if (!matcher.matches()) {
                                                            validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) không phải là số điện thoại hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                        }else {
                                                        	if(phoneMap.containsKey(recipientPhone)) {
                                                        		validatedMessageList.add(
                                                                        String.format("Vị trí (%d, %s) Số điện thoại không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                                );
                                                        	}else{
                                                        		phoneMap.putIfAbsent(recipientPhone, recipientPhone);
                                                        	};
                                                        }
                                                    }
                                        			colIndex = colIndex + 1;
                                        		}
                                        		
                                        		// validate recipient cccd
                                        		if(signType.getId() == SignType.EKYC.getDbVal()) {
                                        			var recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        			if (!StringUtils.hasText(recipientCCCD)){
                                                        validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    }else {
                                                    	var pattern = Pattern.compile(CARID_REGEX);
                                                        var matcher = pattern.matcher(recipientCCCD);

                                                        if (!matcher.matches()) {
                                                            validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) không phải là số CMT/CCCD hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                        }else {
                                                        	if(cccdMap.containsKey(recipientCCCD)) {
                                                        		validatedMessageList.add(
                                                                        String.format("Vị trí (%d, %s) CMT/CCCD không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                                );
                                                        	}else{
                                                        		cccdMap.putIfAbsent(recipientCCCD, recipientCCCD);
                                                        	};
                                                        }
                                                    }
                                                    colIndex = colIndex + 1;
                			                    }else if(signType.getId() == SignType.USB_TOKEN.getDbVal()) {
                			                    	var recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        			if (!StringUtils.hasText(recipientCCCD)){
                                                        validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    }else {
                                                    	var pattern = Pattern.compile(CARID_REGEX);
                                                        var matcher = pattern.matcher(recipientCCCD);

                                                        if (!matcher.matches()) {
                                                        	pattern = Pattern.compile(MST_REGEX);
                                                        	matcher = pattern.matcher(recipientCCCD);
                                                        	
                                                        	if (!matcher.matches()) {
                                                        		validatedMessageList.add(
                                                                        String.format("Vị trí (%d, %s) không phải là số MST/CMT/CCCD hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                                );
                                                        	}
                                                        }else {
                                                        	if(cccdMap.containsKey(recipientCCCD)) {
                                                        		validatedMessageList.add(
                                                                        String.format("Vị trí (%d, %s) CMT/CCCD không được trùng giữa các bên tham gia", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                                );
                                                        	}else{
                                                        		cccdMap.putIfAbsent(recipientCCCD, recipientCCCD);
                                                        	};
                                                        }
                                                    }
                                                    colIndex = colIndex + 1;
                			                    }else if(signType.getId() == SignType.HSM.getDbVal()) {
                			                    	var recipientMST = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        			if (!StringUtils.hasText(recipientMST)){
                                                        validatedMessageList.add(
                                                                String.format("Vị trí (%d, %s) không được để trống", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                        );
                                                    }else {
                                                    	var pattern = Pattern.compile(MST_REGEX);
                                                        var matcher = pattern.matcher(recipientMST);

                                                        if (!matcher.matches()) {
                                                        	validatedMessageList.add(
                                                                    String.format("Vị trí (%d, %s) không phải là số MST hợp lệ", row.getRowNum() + 1, ExcelUtil.columnToLetter(colIndex + 1))
                                                            );
                                                        }
                                                    }
                                                    colIndex = colIndex + 1;
                			                    }
                                        	}
                                        } 
                                	}
                                }
                                
                                participantIndex = participantIndex + 1;
                        	}
                        }
                    }
                    
                    emailMap.clear();
                    participantMap.clear();
                    phoneMap.clear();
    				cccdMap.clear();
                }

                if (validatedMessageList.size() == 0) {
                    validate = ValidateDto.builder()
                            .success(true)
                            .message("template contract uploaded is valid")
                            .detail(null)
                            .build();
                } else {
                    validate = ValidateDto.builder()
                            .success(false)
                            .message("template contract uploaded is invalid")
                            .detail(validatedMessageList)
                            .build();
                }
            } catch (IOException e) {
                log.error("can't open uploaded template file '{}'", temporaryFilePath, e);
            } catch (Exception e) {
                log.error("can't validation uploaded template file '{}'", temporaryFilePath, e);
            }finally {
            	emailMap.clear();
                participantMap.clear();
				contractNoMap.clear();
				contractNoDuplicateMap.clear();
				phoneMap.clear();
				cccdMap.clear();
			}
        }

        return validate;
    }

    public ResponseEntity<Collection<ContractDto>> process(CustomerUser customerUser,
                                                           int templateId,
                                                           int ceCAPush,
                                                           MultipartFile multipartFile,
                                                           boolean deleteTempFile) {
    	var temporaryFilePath = String.format("/tmp/%s.%s", UUID.randomUUID(), FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        //var temporaryFilePath = String.format("D:\\workspace2\\econtract-service\\application\\ec-contract-srv\\tmp/%s.%s", UUID.randomUUID(), FilenameUtils.getExtension(multipartFile.getOriginalFilename()));

        try {
            multipartFile.transferTo(new File(temporaryFilePath));
            return process(customerUser, templateId, ceCAPush, temporaryFilePath);

        } catch (IOException e) {
            log.error("can't write customer upload file \"{}\"", temporaryFilePath, e);
        }finally {
            try {
                if (deleteTempFile) {
                    FileUtils.delete(new File(temporaryFilePath));
                }

            } catch (IOException e) {
                log.error("can't delete temporary file \"{}\"", temporaryFilePath, e);
            }
        }

        return ResponseEntity.internalServerError().build();
    }

    public ResponseEntity<Collection<ContractDto>> process(CustomerUser customerUser,
                                                           int templateId,
                                                           int ceCAPush,
                                                           String filePath) {

        var responseOptional = process(templateId, filePath, customerUser.getId(), ceCAPush);

        if(responseOptional.isPresent()) {
            startBPM(responseOptional.get());
        }

        return responseOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.internalServerError().build()
        );
    }
    /**
     * process imported file
     *
     * @param id                template contract id
     * @param temporaryFilePath temporary file path
     * @param currentUserId     current user
     * @return {@link ContractDto}
     */
    @Transactional
    private Optional<Collection<ContractDto>> process(int id, String temporaryFilePath, int currentUserId, int ceCAPush) {
        var templateContractOptional = templateContractRepository.findById(id);
        if (templateContractOptional.isPresent()) {
            var templateContract = templateContractOptional.get();
            var templateFieldCollection = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(templateContract.getId());
            if (templateFieldCollection != null && templateFieldCollection.size() > 0) {
                templateFieldCollection = templateFieldCollection.stream().filter(
                        field -> (field.getRecipientId() == null || field.getRecipientId() == 0) && field.getType() == FieldType.TEXT
                ).collect(Collectors.toList());
            }

            var contractCollection = new ArrayList<ContractDto>();
            
            var organizationOptional =  customerService.getOrganizationByCustomer(currentUserId);
            
            try {
                var workbook = WorkbookFactory.create(new File(temporaryFilePath));
                var sheet = workbook.getSheetAt(0);
                var totalColumns = sheet.getRow(0).getPhysicalNumberOfCells();
                
                var rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                	var row = rowIterator.next();
                    
                    //Kiểm tra dòng empty or null
                    boolean isRowEmpty = true;
                    for(var columnIndex = 0; columnIndex < totalColumns; columnIndex++) {
                    	try {
                    		var cell = row.getCell(columnIndex, MissingCellPolicy.RETURN_NULL_AND_BLANK); 
                    		
                            if (cell != null && StringUtils.hasText(cell.getStringCellValue()))
                            {
                                isRowEmpty = false;
                                break;
                            }
						} catch (Exception e) {
							// TODO: handle exception
						} 
                    }
                    
                    //Validate từ dòng 1 và không phải dòng empty or null
                    if (row.getRowNum() > 0 && !isRowEmpty) {
                        boolean contractNoInserted = false;
                        var contract = new Contract();
                        // copy template to contract
                        BeanUtils.copyProperties(
                                templateContract,
                                contract,
                                "id", "signTime",
                                "aliasUrl", "notes", "status", "refs",
                                "participants", "reasonReject", "documents"
                        );

                        var contractName = ExcelUtil.getCellValue(row.getCell(0));
                        var contractNo = ExcelUtil.getCellValue(row.getCell(1));
                        var contractSignTime = ExcelUtil.getCellValue(row.getCell(2));

                        contract.setName(contractName);
                        contract.setStatus(ContractStatus.CREATED);
                        
                        try {
                        	contract.setSignTime(dff.parse(contractSignTime));
						} catch (Exception e) {}
                        
                        contract.setCreatedBy(currentUserId);
                        contract.setUpdatedBy(currentUserId);
                        contract.setCustomerId(currentUserId);
                        
                        if(contractNo != null) {
                        	contract.setContractNo(contractNo);
                        }
                        
                        contract.setTemplateContractId(id);
                        contract.setTemplate(true);
                        
                        if(organizationOptional.isPresent()) {
                        	contract.setOrganizationId(organizationOptional.get().getId());
                        }
                         
                        contract.setCeCAPush(ceCAPush);
                        contract.setContractUid(StringUtil.generatePwd(8));

                        // save contract
                        contract = contractRepository.save(contract);

                        // MARK: clone new a document
                        Collection<TemplateDocument> templateDocumentCollection = templateDocumentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                                templateContract.getId(),
                                BaseStatus.ACTIVE.ordinal()
                        );
                        
                        Collection<Document> documentCollection = new ArrayList<>(); 
                        
                        if (templateDocumentCollection.size() > 0) {
                            documentCollection = templateDocumentCollection
                                    .stream().filter(doc ->
                                            (doc.getType() == DocumentType.PRIMARY ||
                                                    doc.getType() == DocumentType.ATTACH)
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
                            }

                            doc.setId(null);
                            doc.setContractId(contract.getId());
                            doc.setCreatedAt(new Date());
                            doc.setUpdatedAt(new Date());
                            doc.setCreatedBy(currentUserId);
                            doc.setUpdatedBy(currentUserId);
                            
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
                                }
                                
                                docTmp.setName(doc.getName());
                                docTmp.setBucket(doc.getBucket());
                                docTmp.setStatus(doc.getStatus());
                                docTmp.setFilename(doc.getFilename());
                                docTmp.setInternal(doc.getInternal());
                                docTmp.setOrdering(doc.getOrdering());
                                
                                docTmp.setId(null);
                                docTmp.setContractId(contract.getId());
                                docTmp.setCreatedAt(new Date());
                                docTmp.setUpdatedAt(new Date());
                                docTmp.setCreatedBy(currentUserId);
                                docTmp.setUpdatedBy(currentUserId);
                                docTmp.setType(DocumentType.FINALLY);
                                
                                documentCollectionClone.add(docTmp);
                        	} 
                        }
                        
                        documentRepository.saveAll(documentCollectionClone);
                        
                        Optional<Document> documentOptinal = documentRepository.findFirstByContractIdAndTypeOrderByCreatedAtDesc(contract.getId(), DocumentType.FINALLY.getDbVal());

                        // MARK: create new a participant
                        if (documentOptinal.isPresent()) {
                        	final Document document = documentOptinal.get();
                        	
                        	Collection<Field> fieldNoActionCollection = new ArrayList<>();
                        	
                            var colIndex = 3;
                            for (var templateField : templateFieldCollection) {
                            	var field = new Field();
                                BeanUtils.copyProperties(
                                        templateField, field,
                                        "id"
                                );
                                
                                var value = ExcelUtil.getCellValue(row.getCell(colIndex));
                                field.setValue(value);
                                fieldNoActionCollection.add(field);
                                
                                colIndex = colIndex + 1;
                            }
                            
                            //Save Tổ chức của tôi
                            var myParticipantOptional = templateParticipantRepository.findMyParticipantByContractId(templateContract.getId());
                            if(myParticipantOptional.isPresent()) {
                            	var myParticipant = myParticipantOptional.get();
                            	
                            	var participant = new Participant();
                                // copy a participant

                                BeanUtils.copyProperties(
                                		myParticipant,
                                        participant,
                                        "id", "recipients", "contractId", "contract"
                                );
                                
                                participant.setContractId(contract.getId());

                                // save participant
                                participant = participantRepository.save(participant);
                                
                                // Field recipentid = null đã add chưa
                                boolean __isAddFieldNull = false;
                                
                                for (var templateRecipient : myParticipant.getRecipients()) {
                                	var recipient = new Recipient();
                                    BeanUtils.copyProperties(
                                            templateRecipient,
                                            recipient,
                                            "id", "participant", "fields", "username", "password"
                                    );

                                    var recipientName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    colIndex = colIndex + 1;
                                    
                                    var recipientCCCD = ""; 
                                    var recipientPhone = "";
                                    var recipientEmail = "";
                                    
                                    if(StringUtils.hasText(recipient.getLoginBy())) {
                                   	 if(recipient.getLoginBy().equals("phone")) {
                                   		recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                   		
                                   		//Đăng nhập qua sđt mặc định trường email = phone
                                   		recipientEmail = recipientPhone;
                                   		
                                        colIndex = colIndex + 1;
                                   	  }else {
                                   		  recipientEmail = ExcelUtil.getCellValue(row.getCell(colIndex));
                                          colIndex = colIndex + 1;
                                   	  }
                                    } 
                                     
                                    TemplateRecipientDto recipientDto = modelMapper.map(
                            				recipient, new TypeToken<TemplateRecipientDto>() {
                                            }.getType()); 
                                    final var signTypeList = recipientDto.getSignType(); 
                                    if (signTypeList != null && signTypeList.size() > 0) {
                                    	for(SignTypeDto signType : signTypeList) {
                                    		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) {
                                    			recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                                colIndex = colIndex + 1; 
                                    		}
                                    		
                                    		if(signType.getId() == SignType.EKYC.getDbVal() 
                                    				|| signType.getId() == SignType.USB_TOKEN.getDbVal()
                                    				|| signType.getId() == SignType.HSM.getDbVal()
                                    			) {
                                    			recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                                colIndex = colIndex + 1;
            			                    }
                                    	}
                                    }
                                    
                                    recipient.setName(recipientName);
                                    recipient.setEmail(recipientEmail);
                                    recipient.setCardId(recipientCCCD);
                                    recipient.setPhone(recipientPhone);
                                    recipient.setParticipant(participant);

                                    if (!customerService.findCustomerByEmail(recipientEmail)) {
                                        var password = StringUtil.generatePwd();
                                        recipient.setUsername(recipientEmail);
                                        recipient.setPassword(password);
                                    }

                                    recipient = recipientRepository.save(recipient);
                                    
                                    // add field recipientId != null
                                    for (var templateField : templateRecipient.getFields()) {
                                        var field = new Field();
                                        BeanUtils.copyProperties(
                                                templateField, field,
                                                "id", "value"
                                        );  

                                        field.setRecipientId(recipient.getId());
                                        field.setDocumentId(document.getId());
                                        field.setContractId(contract.getId());
                                        
                                        if (field.getType() == FieldType.CONTRACT_NO) {
                                        	if(StringUtils.hasText(contractNo)) {
                                        		field.setRecipientId(null); 
                                        	}
                                        	field.setValue(contractNo);
                                            contractNoInserted = true;
                                        }

                                        fieldRepository.save(field);
                                    }
                                    
                                    // add field recipientId == null
                                    if(__isAddFieldNull == false) {
	                                	  for(var field : fieldNoActionCollection) {
	                                          field.setDocumentId(document.getId());
	                                          field.setContractId(contract.getId());
	                                          
	                                          fieldRepository.save(field);
	                                  	}
	                                  	
	                                  	__isAddFieldNull = true;
                                    }
                                }
                            }
                            
                            // Save Đối tác
                            var partnerParticipantCollection = templateParticipantRepository.findPartnerByContractIdOrderById(templateContract.getId());
                            if (partnerParticipantCollection != null && partnerParticipantCollection.size() > 0) {
                            	for (var templateParticipant : partnerParticipantCollection) {
                            		var participant = new Participant();
                                    // copy a participant
                                    BeanUtils.copyProperties(
                                    		templateParticipant,
                                            participant,
                                            "id", "recipients", "contractId", "contract"
                                    );
                                    
                                    var participantName = "";
                                    if (participant.getType() == ParticipantType.ORGANIZATION) {
                                    	participantName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        colIndex = colIndex + 1; 
                                    } else {
                                    	participantName = templateParticipant.getName();
                                    }
                                    
                                    participant.setName(participantName);
                                    participant.setContractId(contract.getId());

                                    // save participant
                                    participant = participantRepository.save(participant);
                                    
                                    // MARK: copy recipient of participant
                                    for (var templateRecipient : templateParticipant.getRecipients()) {
                                    	var recipient = new Recipient();
                                        BeanUtils.copyProperties(
                                                templateRecipient,
                                                recipient,
                                                "id", "participant", "fields", "username", "password"
                                        );

                                        var recipientName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        colIndex = colIndex + 1;

                                        var recipientCCCD = ""; 
                                        var recipientPhone = "";
                                        var recipientEmail = "";
                                        
                                        if(StringUtils.hasText(recipient.getLoginBy())) {
                                       	 if(recipient.getLoginBy().equals("phone")) {
                                       		recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                       		
                                       		//Đăng nhập qua sđt mặc định trường email = phone
                                       		recipientEmail = recipientPhone;
                                       		
                                            colIndex = colIndex + 1;
                                       	  }else {
                                       		  recipientEmail = ExcelUtil.getCellValue(row.getCell(colIndex));
                                              colIndex = colIndex + 1;
                                       	  }
                                        } 
                                         
                                        TemplateRecipientDto recipientDto = modelMapper.map(
                                				recipient, new TypeToken<TemplateRecipientDto>() {
                                                }.getType()); 
                                        final var signTypeList = recipientDto.getSignType(); 
                                        if (signTypeList != null && signTypeList.size() > 0) {
                                        	for(SignTypeDto signType : signTypeList) {
                                        		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) {
                                        			recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                                    colIndex = colIndex + 1; 
                                        		}
                                        		
                                        		if(signType.getId() == SignType.EKYC.getDbVal() 
                                        				|| signType.getId() == SignType.USB_TOKEN.getDbVal()
                                        				|| signType.getId() == SignType.HSM.getDbVal()
                                        			) {
                                        			recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                                    colIndex = colIndex + 1;
                			                    }
                                        	}
                                        }
                                        
                                        recipient.setName(recipientName);
                                        recipient.setEmail(recipientEmail);
                                        recipient.setCardId(recipientCCCD);
                                        recipient.setPhone(recipientPhone);
                                        recipient.setParticipant(participant);

                                        if (!customerService.findCustomerByEmail(recipientEmail)) {
                                            var password = StringUtil.generatePwd();
                                            recipient.setUsername(recipientEmail);
                                            recipient.setPassword(password);
                                        }

                                        //save recipient
                                        recipient = recipientRepository.save(recipient);
                                        
                                        // MARK: copy fields of recipient
                                        for (var templateField : templateRecipient.getFields()) {
                                            var field = new Field();
                                            BeanUtils.copyProperties(
                                                    templateField, field,
                                                    "id", "value"
                                            );  

                                            field.setRecipientId(recipient.getId());
                                            field.setDocumentId(document.getId());
                                            field.setContractId(contract.getId());
                                            
                                            if (field.getType() == FieldType.CONTRACT_NO) {
                                            	if(StringUtils.hasText(contractNo)) {
                                            		field.setRecipientId(null); 
                                            	}
                                            	field.setValue(contractNo);
                                                contractNoInserted = true;
                                            }

                                            fieldRepository.save(field);
                                        }
                                    }
                            	}
                            }
                            
                            if (!contractNoInserted) {
                                var templateContractNoFieldOptional = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(
                                        templateContract.getId()
                                ).stream().filter(tf -> tf.getType() == FieldType.CONTRACT_NO).findFirst();

                                if (templateContractNoFieldOptional.isPresent()) {
                                    var contractNoField = new Field();
                                    BeanUtils.copyProperties(
                                            templateContractNoFieldOptional.get(), contractNoField,
                                            "id", "value"
                                    );

                                    contractNoField.setDocumentId(document.getId());
                                    contractNoField.setContractId(contract.getId());
                                    contractNoField.setValue(contractNo);

                                    fieldRepository.save(contractNoField);
                                }
                            }
                        }

                        contractCollection.add(
                                modelMapper.map(contract, ContractDto.class)
                        );
                    }
                }

                return Optional.of(contractCollection);
            } catch (IOException e) {
                log.error("file '{}' is not found.", temporaryFilePath, e);

                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }

        return Optional.empty();
    }
    
    public void startBPM(Collection<ContractDto> contractCollection) {
    	try {
    		WorkflowDto workflowDto;
            Optional<MessageDto> messageDtoOptional;
            
        	for(ContractDto contract: contractCollection) {
        		processService.byPassContractNo(contract.getId());
                processService.byPassContractUid(contract.getId());

                workflowDto = WorkflowDto
                        .builder()
                        .contractId(contract.getId())
                        .approveType(0)
                        .recipientId(0)
                        .participantId(0)
                        .build();
                
                // Khởi tạo luồng xử lý HĐ
                log.info("start call bpm service for contract id: " + contract.getId());
                messageDtoOptional = bpmService.startWorkflow(workflowDto);
                
                if (messageDtoOptional.isPresent()) {
                    log.info(messageDtoOptional.get().toString());
                    log.info("run BPM contract id: {} success!! status {}",contract.getId(), messageDtoOptional.get().isSuccess());
                }  

                // Cập nhật trạng thái HĐ thành PROCESSING
                if (messageDtoOptional.isPresent() && messageDtoOptional.get().isSuccess()) {
                    log.info("update status");
                    contractService.changeStatus(contract.getId(), ContractStatus.PROCESSING.getDbVal(), null);
                }
        	}
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}  
    }
    
    public ResponseEntity<Collection<ContractDto>> parse(int templateId, MultipartFile multipartFile, boolean deleteTempFile) {
        // lưu tệp tin người dùng tải lên vào thư mục tạm
    	var temporaryFilePath = String.format("/tmp/%s.%s", UUID.randomUUID(), FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        //var temporaryFilePath = String.format("D:\\workspace2\\econtract-service\\application\\ec-contract-srv/%s.%s", UUID.randomUUID(), FilenameUtils.getExtension(multipartFile.getOriginalFilename()));

        try {
            multipartFile.transferTo(new File(temporaryFilePath));

            var contractDtoCollection = parseRow(templateId, temporaryFilePath);

            return ResponseEntity.ok(contractDtoCollection);
        } catch (IOException e) {
            log.error("can't write customer upload file \"{}\"", temporaryFilePath, e);
        } finally {
            try {
                if (deleteTempFile) {
                    FileUtils.delete(new File(temporaryFilePath));
                }

            } catch (IOException e) {
                log.error("can't delete temporary file \"{}\"", temporaryFilePath, e);
            }
        }

        return ResponseEntity.badRequest()
                .build();
    }
    /**
     * parse temporary file to row
     *
     * @param id                template contract id
     * @param temporaryFilePath temporary file path
     * @return {@link ContractDto}
     */
    public Collection<ContractDto> parseRow(int id, String temporaryFilePath) {
    	Collection<ContractDto> contractList = new ArrayList<>();
        var templateContractOptional = templateContractRepository.findById(id);
        if (templateContractOptional.isPresent()) { 
            var templateContract = templateContractOptional.get();
            var templateFieldByContractId = templateFieldRepository.findByContractIdOrderByCoordinateYAsc(templateContract.getId());
            
            //Danh sách ô text chưa gán người xử lý
            Collection<TemplateField> templateFieldCollection = new ArrayList<>();
            if (templateFieldByContractId != null && templateFieldByContractId.size() > 0) {
                templateFieldCollection = templateFieldByContractId.stream().filter(
                        field -> (field.getRecipientId() == null || field.getRecipientId() == 0) && field.getType() == FieldType.TEXT
                ).collect(Collectors.toList()); 
            }
            
            try {
                // read Excel file
                var wookbook = WorkbookFactory.create(new File(temporaryFilePath));
                var sheet = wookbook.getSheetAt(0);
                var totalColumns = sheet.getRow(0).getPhysicalNumberOfCells();

                var rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                    var row = rowIterator.next();
                    
                    //Kiểm tra dòng empty or null
                    boolean isRowEmpty = true;
                    for(var columnIndex = 0; columnIndex < totalColumns; columnIndex++) {
                    	try {
                    		var cell = row.getCell(columnIndex, MissingCellPolicy.RETURN_NULL_AND_BLANK); 
                    		
                            if (cell != null && StringUtils.hasText(cell.getStringCellValue()))
                            {
                                isRowEmpty = false;
                                break;
                            }
						} catch (Exception e) {
							// TODO: handle exception
						} 
                    }
                    
                    //Validate từ dòng 1 và không phải dòng empty or null
                    if (row.getRowNum() > 0 && !isRowEmpty) {
                        // create new a contract
                        var contract = new Contract();
                        // copy template to contract
                        BeanUtils.copyProperties(
                                templateContract,
                                contract,
                                "id", "signTime",
                                "aliasUrl", "notes", "status", "refs",
                                "participants", "reasonReject", "documents"
                        );

                        var contractName = ExcelUtil.getCellValue(row.getCell(0));
                        var contractNo = ExcelUtil.getCellValue(row.getCell(1));
                        var contractEndTime = ExcelUtil.getCellValue(row.getCell(2));
                        
                        // Đọc dữ liệu ô text không có người xử lý
                        var colIndex = 3;
                        if (templateFieldCollection != null && templateFieldCollection.size() > 0) {
                            for (var templateField : templateFieldCollection) {
                                var value = ExcelUtil.getCellValue(row.getCell(colIndex));
                                        
                                templateField.setValue(value);

                                colIndex = colIndex + 1;
                            }
                        }
                        
                        // create new a participant set
                        Set<Participant> participantCollection = new HashSet<>();
                        
                        // Đọc dữ liệu Tổ chức của tôi & add field chưa được gán người xử lý
                        var myParticipantOptional = templateParticipantRepository.findMyParticipantByContractId(templateContract.getId());
                        if(myParticipantOptional.isPresent()) {
                        	var myParticipant = myParticipantOptional.get();
                        	var participant = new Participant();
                        	
                        	BeanUtils.copyProperties(
                        			myParticipant,
                                    participant,
                                    "id", "recipients", "contractId", "contract"
                            );
                        	
                        	// create new a recipient set
                        	Set<Recipient> recipientSet = new HashSet<>();
                        	
                        	// Field recipentid = null đã add chưa
                            boolean __isAddFieldNull = false;
                            
                        	for (var templateRecipient : myParticipant.getRecipients()) {
                        		var recipient = new Recipient();
                                BeanUtils.copyProperties(
                                        templateRecipient,
                                        recipient,
                                        "id", "participant", "fields", "username", "password"
                                );

                                var recipientName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                colIndex = colIndex + 1;

                                var recipientCCCD = ""; 
                                var recipientPhone = "";
                                var recipientEmail = "";
                                
                                if(StringUtils.hasText(recipient.getLoginBy())) {
                               	 if(recipient.getLoginBy().equals("phone")) {
                               		recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                               		
                               		//Đăng nhập qua sđt mặc định trường email = phone
                               		recipientEmail = recipientPhone;
                               		
                                    colIndex = colIndex + 1;
                               	  }else {
                               		  recipientEmail = ExcelUtil.getCellValue(row.getCell(colIndex));
                                      colIndex = colIndex + 1;
                               	  }
                                } 
                                 
                                TemplateRecipientDto recipientDto = modelMapper.map(
                        				recipient, new TypeToken<TemplateRecipientDto>() {
                                        }.getType()); 
                                final var signTypeList = recipientDto.getSignType(); 
                                if (signTypeList != null && signTypeList.size() > 0) {
                                	for(SignTypeDto signType : signTypeList) {
                                		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) {
                                			recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                            colIndex = colIndex + 1; 
                                		}
                                		
                                		if(signType.getId() == SignType.EKYC.getDbVal() 
                                				|| signType.getId() == SignType.USB_TOKEN.getDbVal()
                                				|| signType.getId() == SignType.HSM.getDbVal()
                                			) {
                                			recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                            colIndex = colIndex + 1;
        			                    }
                                	}
                                }
                                
                                // create new a fields set
                                Set<Field> fieldSet = new HashSet<>();
                                
                                // add field recipientId != null
                                for (var templateField : templateRecipient.getFields()) {
                                	var field = new Field();
                                    BeanUtils.copyProperties(
                                            templateField, field,
                                            "id", "value"
                                    );
                                    
                                    if (field.getType() == FieldType.CONTRACT_NO) {
                                        field.setValue(contractNo);
                                    }

                                    fieldSet.add(field);
                                }
                                
                                // add field recipientId == null (chưa gán người xử lý)
                                if(__isAddFieldNull == false) {
                                	//ô text chưa người xử lý
                                	for(var templateField : templateFieldCollection) {
                                		var field = new Field();
                                        BeanUtils.copyProperties(
                                                templateField, field,
                                                "id"
                                        );
                                        
                                        fieldSet.add(field);
                                	}
                                	
                                	//ô số hợp đồng chưa gán người xử lý 
                                	var fieldContractNoOptional = templateFieldByContractId.stream().filter(
                                			field -> (field.getRecipientId() == null || field.getRecipientId() == 0) && field.getType() == FieldType.CONTRACT_NO
                                    ).findFirst();

                                    if (fieldContractNoOptional.isPresent()) {
                                    	var field = new Field();
                                        BeanUtils.copyProperties(
                                        		fieldContractNoOptional.get(), field,
                                                "id"
                                        );
                                        
                                        field.setValue(contractNo);
                                        
                                        fieldSet.add(field); 
                                    }
                                	
                                	__isAddFieldNull = true;
                                }
                                
                                recipient.setName(recipientName);
                                recipient.setEmail(recipientEmail);
                                recipient.setPhone(recipientPhone);
                                recipient.setCardId(recipientCCCD);
                                recipient.setFields(fieldSet);
                                recipientSet.add(recipient);
                        	}
                        	
                            participant.setRecipients(recipientSet);
                            participantCollection.add(participant);
                        }
                        
                        // Đọc dữ liệu đối tác
                        var partnerParticipantCollection = templateParticipantRepository.findPartnerByContractIdOrderById(templateContract.getId());
                        if (partnerParticipantCollection != null && partnerParticipantCollection.size() > 0) {
                        	for (var templateParticipant : partnerParticipantCollection) {
                        		// create new a participant
                                var participant = new Participant();
                                BeanUtils.copyProperties(
                                        templateParticipant,
                                        participant,
                                        "id", "recipients", "contractId", "contract"
                                );
                                
                                // Đọc dữ liệu tên đối tác
                                var participantName = "";
                                if (participant.getType() == ParticipantType.ORGANIZATION) {
                                	participantName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    colIndex = colIndex + 1;
                                } else {
                                	participantName = templateParticipant.getName(); 
                                }
                                
                                // create new a recipient set
                                Set<Recipient> recipientSet = new HashSet<>();
                                
                                for (var templateRecipient : templateParticipant.getRecipients()) {
                                	var recipient = new Recipient();
                                    BeanUtils.copyProperties(
                                            templateRecipient,
                                            recipient,
                                            "id", "participant", "fields", "username", "password"
                                    );

                                    var recipientName = ExcelUtil.getCellValue(row.getCell(colIndex));
                                    colIndex = colIndex + 1;

                                    var recipientCCCD = ""; 
                                    var recipientPhone = "";
                                    var recipientEmail = "";
                                    
                                    if(StringUtils.hasText(recipient.getLoginBy())) {
                                   	 if(recipient.getLoginBy().equals("phone")) {
                                   		recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                   		
                                   		//Đăng nhập qua sđt mặc định trường email = phone
                                   		recipientEmail = recipientPhone;
                                   		
                                        colIndex = colIndex + 1;
                                   	  }else {
                                   		  recipientEmail = ExcelUtil.getCellValue(row.getCell(colIndex));
                                          colIndex = colIndex + 1;
                                   	  }
                                    } 
                                     
                                    TemplateRecipientDto recipientDto = modelMapper.map(
                            				recipient, new TypeToken<TemplateRecipientDto>() {
                                            }.getType()); 
                                    final var signTypeList = recipientDto.getSignType(); 
                                    if (signTypeList != null && signTypeList.size() > 0) {
                                    	for(SignTypeDto signType : signTypeList) {
                                    		if(signType.getId() == SignType.IMAGE_AND_OTP.getDbVal() && recipient.getLoginBy().equals("email")) {
                                    			recipientPhone = ExcelUtil.getCellValue(row.getCell(colIndex));
                                                colIndex = colIndex + 1; 
                                    		}
                                    		
                                    		if(signType.getId() == SignType.EKYC.getDbVal() 
                                    				|| signType.getId() == SignType.USB_TOKEN.getDbVal()
                                    				|| signType.getId() == SignType.HSM.getDbVal()
                                    			) {
                                    			recipientCCCD = ExcelUtil.getCellValue(row.getCell(colIndex));
                                                colIndex = colIndex + 1;
            			                    }
                                    	}
                                    }

                                    // create new a fields set
                                    Set<Field> fieldSet = new HashSet<>();
                                    
                                    for (var templateField : templateRecipient.getFields()) {
                                        var field = new Field();
                                        BeanUtils.copyProperties(
                                                templateField, field,
                                                "id", "value"
                                        );

                                        if (field.getType() == FieldType.CONTRACT_NO) {
                                            field.setValue(contractNo);
                                        }

                                        fieldSet.add(field);
                                    }
                                    
                                    recipient.setName(recipientName);
                                    recipient.setEmail(recipientEmail);
                                    recipient.setPhone(recipientPhone);
                                    recipient.setCardId(recipientCCCD);
                                    recipient.setFields(fieldSet);
                                    recipientSet.add(recipient);
                                }
                                
                                participant.setName(participantName);
                                participant.setRecipients(recipientSet);
                                participantCollection.add(participant);
                        	}
                        }
                        
                        contract.setName(contractName);
                        
                        try {
                        	contract.setSignTime(dff.parse(contractEndTime));
                        }catch (Exception e) {
							// TODO: handle exception
						}
                         
                        contract.setStatus(ContractStatus.CREATED);
                        contract.setParticipants(participantCollection);
                        contractList.add(
                                modelMapper.map(contract, ContractDto.class)
                        );
                    }
                }
            } catch (IOException e) {
                log.error("excel file '{}' not found", temporaryFilePath, e);
            }
        }

        return contractList;
    }
    
    private void createCellHeader(XSSFRow headerRow, int colIndex, XSSFCellStyle cellStyle, String value) {
    	var headerCell = headerRow.createCell(colIndex, CellType.STRING);
    	headerCell.setCellValue(value);
    	headerCell.setCellStyle(cellStyle);
    }
}
