package com.vhc.ec.contract.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.http.client.methods.HttpPost;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.dto.CeCARequest;
import com.vhc.ec.contract.dto.CeCAResponse;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.entity.CeCALog;
import com.vhc.ec.contract.entity.Document;
import com.vhc.ec.contract.repository.CeCALogRepository;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CeCAService {
	private final ContractRepository contractRepository;
	private final DocumentRepository documentRepository;
	private final FileService fileService;
	private final CeCALogRepository ceCALogRepository; 
	private final ContractService contractService;
	private final DocumentService documentService;

	@Value("${vhc.ec.mobifone.ceca-service.request-to-ceca-url}")
	private String requestToCeCAUrl;

	final String API_KEY = "35807EFF-FD75-48F8-8265-65277AEF1DB5";

	/**
	 * Lấy file hợp đồng ở trạng thái hoàn thành
	 * 
	 * @param contractId
	 * @return
	 */
	public Optional<CeCAResponse> requestCeCA(int contractId) {
		final var contractOptional = contractRepository.findById(contractId);

		if (contractOptional.isPresent()) {
			// Lấy thông tin file hợp đồng đã hoàn thành(file đã ký)
			final var docOptional = documentRepository.findFirstByContractIdAndTypeOrderByCreatedAtDesc(contractId,
					DocumentType.FINALLY.getDbVal());

			if (docOptional.isPresent()) {
				try {
					Document doc = docOptional.get();

					// Get file từ MinIO
					final String hexEncodedBytes = fileService.getHexToFileMinIO(doc.getBucket(), doc.getPath()); 

					// Gen messageId
					final String SENDER_ID = "MOBIFONE";
					final String messageId = StringUtil.generateMessageId(SENDER_ID);

					var restTemplate = new RestTemplate();

					CeCARequest ceCARequest = CeCARequest.builder().hexEncodeFile(hexEncodedBytes).senderId(SENDER_ID)
							.messageId(messageId).build();

					// Lưu log request
					var requestLog = new CeCALog();
					BeanUtils.copyProperties(ceCARequest, requestLog, "hexEncodeFile");
					requestLog.setMessage("eContract gửi request lên CeCA");
					requestLog.setContractId(contractId);
					requestLog.setCreatedBy(0);
					requestLog.setCreatedAt(new Date());
					requestLog.setSendDate(new Date());

					ceCALogRepository.save(requestLog);

					HttpHeaders headers = new HttpHeaders();
					headers.set("X-API-KEY", API_KEY);

					HttpEntity<CeCARequest> requestBody = new HttpEntity<>(ceCARequest, headers);
					CeCAResponse response = restTemplate
							.postForEntity(requestToCeCAUrl, requestBody, CeCAResponse.class).getBody();

					// Lưu log kết quả
					var reponseLog = new CeCALog();
					BeanUtils.copyProperties(response, reponseLog);
					reponseLog.setStatus(Integer.valueOf(response.getStatus()));
					reponseLog.setContractId(contractId);
					reponseLog.setCreatedBy(0);
					reponseLog.setCreatedAt(new Date());
					reponseLog.setSenderId("CECA");
					reponseLog.setSendDate(response.getSendDate());

					ceCALogRepository.save(reponseLog);
					
					
					// Cập nhật trạng thái CECA
					contractService.updateCeCAStatus(contractId, Integer.valueOf(response.getStatus())); 

					return Optional.of(response);
				} catch (Exception e) {
					log.error("" + e);
				}
			}

		}

		return Optional.empty();
	}

	/**
	 * Nhận resquest từ CeCA
	 * @param ceCARequest
	 * @return
	 */
	public MessageDto receiveCeCA(CeCAResponse ceCARequest) {
		//
		log.info("==> CeCA gửi phản hồi referenceMessageId = "+ ceCARequest.getReferenceMessageId());
		log.info("==> transactionId = "+ceCARequest.getTransactionId()); 
		log.info("==> status = "+ceCARequest.getStatus());
		log.info("==> message = "+ceCARequest.getStatus());
		
		final var ceCALogOptional = ceCALogRepository.findByMessageId(ceCARequest.getReferenceMessageId());

		if (ceCALogOptional.isPresent()) {
			final var ceCALog = ceCALogOptional.get();

			final var contractOptional = contractRepository.findById(ceCALog.getContractId());

			if (contractOptional.isPresent()) {
				final var contract = contractOptional.get(); 

				// Lấy thông tin file hợp đồng đã hoàn thành(file đã ký)
				final var docOptional = documentRepository.findFirstByContractIdAndTypeOrderByCreatedAtDesc(
						contract.getId(), DocumentType.FINALLY.getDbVal());

				if (docOptional.isPresent()) {
					Collection<Document> docCollection = new ArrayList<Document>();
					try {
						//Xóa file nén trước đó
						documentService.deleteByContractIdAndType(contract.getId(), DocumentType.COMPRESS.getDbVal());
						
						// Chuyển loại file từ FINALLY --> BACKUP(lưu file hợp đồng hoàn thành trước khi
						// đẩy lên CeCA)
						Document docFinally = docOptional.get();
						docFinally.setType(DocumentType.BACKUP);
						docFinally.setUpdatedAt(new Date()); 
						docFinally.setUpdatedBy(docFinally.getCreatedBy());
						docCollection.add(docFinally);
						
						// Đẩy file lên MinIO
						final var fileInfo = fileService.receiveCeCA(ceCARequest.getHexEncodeFile(), contract.getOrganizationId(), docFinally.getFilename());

						// Lưu thông tin file hợp đồng hoàn thành nhận được từ CeCA
						Document doc = new Document();
						doc.setName(contract.getName());
						
						final var fileExtend = fileInfo.getFilename().substring(fileInfo.getFilename().lastIndexOf("."));
						final var fileName = fileInfo.getFilename().substring(0, fileInfo.getFilename().lastIndexOf("."));
						doc.setFilename(String.format("%s_xac thuc BCT%s", fileName, fileExtend));
						doc.setInternal(1);
						doc.setPath(fileInfo.getFilePath());
						doc.setBucket(fileInfo.getBucket());
						doc.setType(DocumentType.FINALLY);
						doc.setContractId(contract.getId());
						doc.setStatus(BaseStatus.ACTIVE);
						doc.setCreatedBy(docFinally.getCreatedBy());
						doc.setCreatedAt(new Date());
						doc.setUpdatedBy(docFinally.getCreatedBy());
						doc.setUpdatedAt(new Date());  
						
						docCollection.add(doc);
						
						documentRepository.saveAll(docCollection);
						 
						// Lưu log kết quả
						var cecaLog = new CeCALog();
						cecaLog.setReferenceMessageId(ceCARequest.getReferenceMessageId());
						cecaLog.setTransactionId(ceCARequest.getTransactionId());
						cecaLog.setSenderId("CECA");
						cecaLog.setSendDate(ceCARequest.getSendDate());
						cecaLog.setMessageId(ceCARequest.getMessageId());
						cecaLog.setMessage(ceCARequest.getMessage());
						cecaLog.setStatus(Integer.valueOf(ceCARequest.getStatus()));
						cecaLog.setContractId(contract.getId());
						cecaLog.setCreatedBy(0);
						cecaLog.setCreatedAt(new Date());
						
						ceCALogRepository.save(cecaLog);
						
						// Cập nhật trạng thái CECA
						contractService.updateCeCAStatus(contract.getId(), Integer.valueOf(ceCARequest.getStatus()));
						
						return MessageDto.builder()
								.success(true)
								.message(ceCARequest.getMessage())
								.build();
					} catch (Exception e) {
						log.error("" + e);
					}
				}
			} 
		}
		
		return MessageDto.builder().success(false).message("Unexpected error").build();
	}
}
