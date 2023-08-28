package com.vhc.ec.contract.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.entity.Field;
import com.vhc.ec.contract.repository.DocumentRepository;
import com.vhc.ec.contract.repository.FieldRepository;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.Loader;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignService {

    private final FileService fileService;
    private final RecipientRepository recipientRepository;
    private final DocumentRepository documentRepository;
    private final FieldRepository fieldRepository;
    private final ModelMapper modelMapper;
    //
    private final CloseableHttpClient httpClient;
    
    @Value("${vhc.ec.mobifone.sign-service.sign-api-url}")
    private String signApiUrl;
    
    @Value("${vhc.ec.mobifone.sign-service.download-api-url}")
    private String downloadApiUrl;

    @Value("${vhc.ec.mobifone.sign-service.sign-api-verify-url}")
    private String signApiVerifyUrl;
    
    @Value("${vhc.ec.mobifone.sign-service.hsm-auth-api-url}")
    private String hsmAuthUrl;
    
    @Value("${vhc.ec.mobifone.sign-service.hsm-sign-api-url}")
    private String hsmSignUrl;

    @Value("${vhc.ec.mobifone.sign-service.sign-api-url-v3}")
    private String signApiUrlv3;

    @Value("${vhc.ec.mobifone.sign-service.sign-api-pkcs-url-create-signature}")
    private String pkcsCreateTokenUrl;

    @Value("${vhc.ec.temporary.directory}")
    private String tempFolder;

    @Transactional
    public MessageDto simPkiSignV3(int recipientId, String mobile, String networkCode, String prompt, String reason, String imageBase64) {
        final var recipientOptional = recipientRepository.findById(recipientId);
        if (recipientOptional.isPresent()) {
            final var recipient = recipientOptional.get();

            var contractId = recipient.getParticipant().getContractId();
            final var docCollection = documentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                    contractId, BaseStatus.ACTIVE.ordinal()
            );
            final var docOptional = docCollection.stream().filter(
                    document -> document.getType() == DocumentType.FINALLY
            ).findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();
                final var presignedUrl = fileService.getPresignedObjectUrl(
                        doc.getBucket(),
                        doc.getPath()
                );

                //Lấy thông tin ô ký số
                final var fieldOptinal = fieldRepository.findFirstByRecipientIdAndType(recipientId, FieldType.DIGITAL_SIGN);

                if(fieldOptinal.isEmpty()) {
                    return MessageDto.builder()
                            .success(false)
                            .message("Can't find sign field")
                            .build();
                }

                if(fieldOptinal.isPresent()) {
                    final var field = fieldOptinal.get();

                    final var request = new HttpPost(signApiUrlv3);
                    request.setHeader("TenantCode", "mobifone.vn");

                    try {
                        var bytes = IOUtils.toByteArray(
                                new URL(presignedUrl)
                        );

                        // chuyển trục tọa độ trước khi gọi ký
                        var coordinateOptional = convertCoordinateToPKI(field, presignedUrl);

                        if(coordinateOptional.isEmpty()) {
                            MessageDto.builder()
                                    .success(false)
                                    .message("Convert Coordinate error")
                                    .build();
                        }

                        var coordinate = coordinateOptional.get();

                        log.info("Tọa độ cần ký SimPKI: {}", coordinate);

                        //chuyển base64 to Hex
                        var hexImage = StringUtil.base64ToHex(imageBase64);

                        if(!StringUtils.hasText(hexImage)){
                            MessageDto.builder()
                                    .success(false)
                                    .message("Convert file image error")
                                    .build();
                        }

                        var entity = MultipartEntityBuilder.create()
                                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                                .addBinaryBody("file", bytes, ContentType.DEFAULT_BINARY, doc.getFilename())
                                .addTextBody("msisdn", mobile)
                                .addTextBody("networkCode", networkCode)
                                .addTextBody("prompt", StringUtils.hasText(prompt) ? removeAccent(prompt) : "n/a")
                                .addTextBody("reason", StringUtils.hasText(reason) ? reason : "n/a")
                                .addTextBody("toX", String.valueOf(coordinate.getCoordinateX()))
                                .addTextBody("toY", String.valueOf(coordinate.getCoordinateY()))
                                .addTextBody("toW", String.valueOf(coordinate.getWidth()))
                                .addTextBody("toH", String.valueOf(coordinate.getHeight()))
                                .addTextBody("pageNumber", String.valueOf(coordinate.getPage()))
                                .addTextBody("hexImage", hexImage)
                                .build();

                        request.setEntity(entity);

                        log.info("start call sign api");
                        final var response = httpClient.execute(request);
                        var responseEntity = response.getEntity();
                        String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                        log.info("call sign api done, body: " + responseBody);

                        if (response.getStatusLine().getStatusCode() == 200) {
                            log.info("call sign api success");
                            final var jsonString = responseBody;
                            final var objectMapper = new ObjectMapper();

                            final var jsonNode = objectMapper.readTree(jsonString);
                            if (jsonNode.has("id") && jsonNode.has("fileName")) {
                                final var mbfDocId = jsonNode.get("id").textValue();
                                final var mbfFileName = jsonNode.get("fileName").textValue();

                                boolean success = repaceFile(contractId, mbfFileName, mbfDocId);
                                if (success) {
                                    log.info("sign contract success");
                                    return MessageDto.builder()
                                            .success(true)
                                            .message("successful")
                                            .build();
                                }
                            }

                            return MessageDto.builder()
                                    .success(false)
                                    .message(jsonString)
                                    .build();
                        }
                    } catch (IOException e) {
                        log.error("can't get file from url = " + presignedUrl, e);
                    }
                }
            }
        }

        return MessageDto.builder()
                .success(false)
                .message("Unexpected error")
                .build();
    }

    @Transactional
    public MessageDto simPkiSignV2(int recipientId, String mobile, String networkCode, String prompt, String reason) {
        final var recipientOptional = recipientRepository.findById(recipientId);
        if (recipientOptional.isPresent()) {
            final var recipient = recipientOptional.get();

            var contractId = recipient.getParticipant().getContractId();
            final var docCollection = documentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                    contractId, BaseStatus.ACTIVE.ordinal()
            );
            final var docOptional = docCollection.stream().filter(
                    document -> document.getType() == DocumentType.FINALLY
            ).findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();
                final var presignedUrl = fileService.getPresignedObjectUrl(
                        doc.getBucket(),
                        doc.getPath()
                );

                final var request = new HttpPost(signApiUrl);
                request.setHeader("TenantCode", "mobifone.vn");

                try {
                    var bytes = IOUtils.toByteArray(
                            new URL(presignedUrl)
                    );

                    var entity = MultipartEntityBuilder.create()
                            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                            .addBinaryBody("file", bytes, ContentType.DEFAULT_BINARY, doc.getFilename())
                            .addTextBody("msisdn", mobile)
                            .addTextBody("networkCode", networkCode)
                            .addTextBody("prompt", StringUtils.hasText(prompt) ? removeAccent(prompt) : "n/a")
                            .addTextBody("reason", StringUtils.hasText(reason) ? reason : "n/a")
                            .build();

                    request.setEntity(entity);

                    log.info("start call sign api");
                    final var response = httpClient.execute(request);
                    var responseEntity = response.getEntity();
                    String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                    log.info("call sign api done, body: " + responseBody);

                    if (response.getStatusLine().getStatusCode() == 200) {
                        log.info("call sign api success");
                        final var jsonString = responseBody;
                        final var objectMapper = new ObjectMapper();

                        final var jsonNode = objectMapper.readTree(jsonString);
                        if (jsonNode.has("id") && jsonNode.has("fileName")) {
                            final var mbfDocId = jsonNode.get("id").textValue();
                            final var mbfFileName = jsonNode.get("fileName").textValue();

                            boolean success = repaceFile(contractId, mbfFileName, mbfDocId);
                            if (success) {
                                log.info("sign contract success");
                                return MessageDto.builder()
                                        .success(true)
                                        .message("successful")
                                        .build();
                            }
                        }

                        return MessageDto.builder()
                                .success(false)
                                .message(jsonString)
                                .build();
                    }
                } catch (IOException e) {
                    log.error("can't get file from url = " + presignedUrl, e);
                }
            }
        }

        return MessageDto.builder()
                .success(false)
                .message("Unexpected error")
                .build();
    }

    public List<SignatureInfoDto> signatureInfo(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (IOException e) {
            log.error("can't upload file", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        var restTemplate = new RestTemplate();
        Object[] objects = restTemplate.postForEntity(signApiVerifyUrl, requestEntity, Object[].class)
                .getBody();
        file.delete();
        return Arrays.stream(objects).map(obj -> modelMapper.map(obj, SignatureInfoDto.class))
                .collect(Collectors.toList());
    }

    public PkcsCreateTokenResponse createPcksEmptyToken(int recipientId, PkcsCreateTokenRequest pkcsCreateTokenRequest) throws MalformedURLException {
        var recipient  = recipientRepository.findById(recipientId).orElse(null);

        if (recipient == null) {
            return PkcsCreateTokenResponse.builder().message("Cannot find recipient").build();
        }

//        var field = fieldRepository.findById(pkcsCreateTokenRequest.getFieldId()).orElse(null);
//        if (field == null || field.getRecipientId() != recipientId) {
//            return PkcsCreateTokenResponse.builder().message("Invalid field").build();
//        }

        int contractId = recipient.getParticipant().getContractId();
        final var docCollection = documentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                contractId, BaseStatus.ACTIVE.ordinal()
        );

        var doc = docCollection.stream().filter(
                document -> document.getType() == DocumentType.FINALLY
        ).findFirst().orElse(null);

        if (doc == null) {
            doc = docCollection.stream().filter(
                    document -> document.getType() == DocumentType.PRIMARY
            ).findFirst().orElse(null);
        }

        if (doc == null) {
            return PkcsCreateTokenResponse.builder().message("Cannot find contract document").build();
        }

        String presignedUrl = fileService.getPresignedObjectUrl(
                doc.getBucket(),
                doc.getPath()
        );

        var restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("TenantCode", "mobifone.vn");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new UrlResource(presignedUrl));
        body.add("toX", pkcsCreateTokenRequest.getX());
        body.add("toY", pkcsCreateTokenRequest.getY());
        body.add("toW", pkcsCreateTokenRequest.getWidth());
        body.add("toH", pkcsCreateTokenRequest.getHeight());
        body.add("pageNumber", pkcsCreateTokenRequest.getPage());
        body.add("hexImage", StringUtil.base64ToHex(pkcsCreateTokenRequest.getImage()));

        body.add("base64Cert", pkcsCreateTokenRequest.getCert());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(pkcsCreateTokenUrl, requestEntity, PkcsCreateTokenResponse.class);
    }

    private boolean repaceFile(int contractId, String mbfDocName, String mbfDocId) {
        final var tempFolder = "/tmp/" + UUID.randomUUID();

        InputStream is = null;
        FileOutputStream fos = null;

        try {
            // make a directory
            FileUtils.forceMkdir(new File(tempFolder));

            final var request = new HttpGet(downloadApiUrl + "?signed_doc_id=" + mbfDocId);
            request.setHeader("TenantCode", "mobifone.vn");

            final var response = httpClient.execute(request);

            is = response.getEntity().getContent();
            fos = new FileOutputStream(tempFolder + "/" + mbfDocName);

            // download file
            int inByte;
            while ((inByte = is.read()) != -1) {
                fos.write(inByte);
            }

            // get document
            final var documentCollection = documentRepository
                    .findAllByContractIdAndStatusOrderByTypeDesc(
                            contractId, BaseStatus.ACTIVE.ordinal()
                    );
            final var docOptional = documentCollection.stream().filter(
                    document -> document.getType() == DocumentType.FINALLY
            ).findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();

                final var uploadFileDtoOptional = fileService
                        .replace(tempFolder + "/" + mbfDocName, doc.getBucket(), doc.getPath());
                return uploadFileDtoOptional.isPresent();
            }
        } catch (IOException e) {
            log.error("can't download file from MBF, docId = {} and docName = {}", mbfDocId, mbfDocName, e);

            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    log.error("can't close input stream", ex);
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    log.error("can't close file output stream");
                }
            }

            try {
                FileUtils.deleteDirectory(new File(tempFolder));
            } catch (IOException ex) {
                log.error("can't delete directory, path = {}", tempFolder);
            }
        }

        return false;
    }

    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(temp).replaceAll("")
                .replaceAll("Đ", "D")
                .replace("đ", "d");
    }
    
    @Transactional
    public MessageDto hsmSign(int recipientId, HsmSignRequest hsmSignRequest) {

        final var recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return MessageDto.builder()
                    .success(false)
                    .message("Can't not find recipient!")
                    .build();
        }
    	//Get token
    	//Get token
    	final var hsmAuthOptinal = hsmAuth(hsmSignRequest.getUsername(), hsmSignRequest.getPassword(), hsmSignRequest.getTaxCode());

    	try {
    		if(hsmAuthOptinal.isPresent()) {
        		final var hsmAuth= hsmAuthOptinal.get();
        		
        		if(StringUtils.hasText(hsmAuth.getToken())) {
                    var contractId = recipient.getParticipant().getContractId();
                    final var docCollection = documentRepository.findAllByContractIdAndStatusOrderByTypeDesc(
                            contractId, BaseStatus.ACTIVE.ordinal()
                    );

                    //Lấy thông tin file hợp đồng
                    final var docOptional = docCollection.stream().filter(
                            document -> document.getType() == DocumentType.FINALLY
                    ).findFirst();

                    if(docOptional.isEmpty()) {
                        return MessageDto.builder()
                                .success(false)
                                .message("Can't find file contract")
                                .build();
                    }

                    if (docOptional.isPresent()) {
                        final var doc = docOptional.get();
                        final var presignedUrl = fileService.getPresignedObjectUrl(
                                doc.getBucket(),
                                doc.getPath()
                        );

                        //Lấy thông tin ô ký số
                        final var fieldOptinal = fieldRepository.findFirstByRecipientIdAndType(recipientId, FieldType.DIGITAL_SIGN);

                        if(fieldOptinal.isEmpty()) {
                            return MessageDto.builder()
                                    .success(false)
                                    .message("Can't find sign field")
                                    .build();
                        }

                        //Gọi API Ký HSM và replace file hợp đồng sau khi ký
                        if(fieldOptinal.isPresent()) {
                            final var field = fieldOptinal.get();

                            String signMessage = replaceFileAfterHsmSign(field, contractId, presignedUrl, doc.getFilename(),
                                    hsmAuth.getToken(), hsmSignRequest.getTaxCode(), hsmSignRequest.getPassword2(), hsmSignRequest.getImageBase64());

                            if (signMessage.equals("success")) {
                                log.info("sign contract success");
                                return MessageDto.builder()
                                        .success(true)
                                        .message("successful")
                                        .build();
                            }

                            return MessageDto.builder()
                                    .success(false)
                                    .message(signMessage)
                                    .build();
                        }
                    }

        		}
        		
        		return MessageDto.builder()
    	                .success(false)
    	                .message(hsmAuth.getError())
    	                .build();
        	} 
		} catch (Exception e) {
			log.error(e.toString());
		} 

        return MessageDto.builder()
                .success(false)
                .message("Unexpected error")
                .build();
    }
    
    @Transactional
    public Optional<HsmAuthReponse> hsmAuth(String username, String password, String taxCode) {
    	try {
    		var restTemplate = new RestTemplate();
        	
        	var hsmAuthRequest = HsmAuthRequest.builder()
        			.username(username)
        			.password(password)
        			.taxCode(taxCode)
        			.build();
        	
        	HttpEntity<HsmAuthRequest> requestBody = new HttpEntity<>(hsmAuthRequest);

        	HsmAuthReponse response = restTemplate
    				.postForEntity(hsmAuthUrl, requestBody, HsmAuthReponse.class).getBody();
        	
        	return Optional.of(response);
		} catch (Exception e) {
			log.error("Can't call api HSM auth {}",e);
		} 
    	
    	return Optional.empty();
    }
    
    /**
     * Replace file hợp đồng sau khi ký
     * 
     */
    private String replaceFileAfterHsmSign(Field field,  int contractId, String presignedUrl, String fileName, 
    		String token, String taxCode, String password2, String imageBase64) {
        final var tempFolder = "./tmp/" + UUID.randomUUID();
    	//final var tempFolder = String.format("D:\\workspace2\\econtract-service\\application\\ec-contract-srv/tmp/%s", UUID.randomUUID());
    	
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            // make a directory
            FileUtils.forceMkdir(new File(tempFolder));

            final var request = new HttpPost(hsmSignUrl); 
            
            System.out.println("hsmSignUrl="+hsmSignUrl);
            System.out.println("Author = "+token +";"+taxCode);
            request.setHeader("Authorization", "Bear "+ token +";"+taxCode); 
             
            var bytes = IOUtils.toByteArray(
                    new URL(presignedUrl)
            );
            
            // chuyển trục tọa độ trước khi gọi ký HSM
            var hsmCoordinateOptional = convertCoordinateToHSM(field, presignedUrl);
            
            if(hsmCoordinateOptional.isEmpty()) {
            	return "false";
            }
            
            var hsmCoordinate = hsmCoordinateOptional.get();
            
            log.info("Tọa độ cần ký: {}", hsmCoordinate); 
            
            var entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", bytes, ContentType.DEFAULT_BINARY, fileName)
                    .addTextBody("password2", password2)
                    .addTextBody("fieldName", "C10008MOBIFONE")
                    .addTextBody("digestAlgorithms", "SHA-256")
                    .addTextBody("toX", String.valueOf(hsmCoordinate.getCoordinateX()))
                    .addTextBody("toY", String.valueOf(hsmCoordinate.getCoordinateY()))
                    .addTextBody("toW", String.valueOf(hsmCoordinate.getWidth()))
                    .addTextBody("toH", String.valueOf(hsmCoordinate.getHeight()))
                    .addTextBody("toP", String.valueOf(hsmCoordinate.getPage()))
                    .addTextBody("imageBase64", imageBase64)
                    .build();
			
			/**
            var entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", bytes, ContentType.DEFAULT_BINARY, fileName)
                    .addTextBody("password2", password2)
                    .addTextBody("fieldName", "C10008MOBIFONE")
                    .addTextBody("digestAlgorithms", "SHA-256")
                    .addTextBody("toX", "10")
                    .addTextBody("toY", "10")
                    .addTextBody("toW", "150")
                    .addTextBody("toH", "100")
                    .addTextBody("toP", "1")
                    .addTextBody("imageBase64", imageBase64)
                    .build();
            */
            request.setEntity(entity);	

            log.info("start call sign api hsm");
            
            final var response = httpClient.execute(request);
             
            //String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            //System.out.println("responseBody = "+responseBody);
            log.info("StatusCode: {}", response.getStatusLine().getStatusCode());

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	is = response.getEntity().getContent();
                fos = new FileOutputStream(tempFolder + "/" + fileName);

                // download file
                int inByte;
                while ((inByte = is.read()) != -1) {
                    fos.write(inByte);
                }

                // get document
                final var documentCollection = documentRepository
                        .findAllByContractIdAndStatusOrderByTypeDesc(
                                contractId, BaseStatus.ACTIVE.ordinal()
                        );
                final var docOptional = documentCollection.stream().filter(
                        document -> document.getType() == DocumentType.FINALLY
                ).findFirst();

                if (docOptional.isPresent()) {
                    final var doc = docOptional.get();

                    final var uploadFileDtoOptional = fileService
                            .replace(tempFolder + "/" + fileName, doc.getBucket(), doc.getPath());
                    
                    if(uploadFileDtoOptional.isPresent()) {
                    	return "success";
                    }else {
                    	return "false";
                    }
                }
            }else {
                log.info("response = {}", EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            	return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("can't push contract file sign HSM {}", e); 
        }finally {
        	if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    log.error("can't close input stream", ex);
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    log.error("can't close file output stream");
                }
            }
            
            try {
                FileUtils.deleteDirectory(new File(tempFolder));
            } catch (IOException ex) {
                log.error("can't delete directory, path = {}", tempFolder);
            }
		}

        return "false";
    }

    public List<MyProcessResPageInfo> getPageInfo(int documentId) {
        var document = documentRepository.findById(documentId).orElse(null);
        List<MyProcessResPageInfo> pageInfoList = new ArrayList<>();

        if (document != null) {

            String presignedUrl = fileService.getPresignedObjectUrl(
                    document.getBucket(),
                    document.getPath()
            );

            try (final var pdf = Loader.loadPDF(new URL(presignedUrl).openStream())) {
                for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                    var page = pdf.getPage(i);
                    var pageInfo = new MyProcessResPageInfo();
                    pageInfoList.add(pageInfo);
                    pageInfo.setWidth(page.getMediaBox().getWidth());
                    pageInfo.setHeight(page.getMediaBox().getHeight());
                    pageInfo.setPage(i + 1);
                }

            } catch (IOException e) {
                log.error(String.format("can't load pdf at \"%s\"", presignedUrl), e);
            }
        }

        return pageInfoList;
    }

    public boolean isValidMutliHsmSignReq(MultiHsmSignReq req) {
        for (int recipientId : req.getRecipients()) {
            var recipient = recipientRepository.findById(recipientId).orElse(null);
            if (recipient != null && !recipient.getCardId().equals(req.getHsmSignRequest().getTaxCode())) {
                return false;
            }
        }

        return true;
    }
    /**
     * 
     * @param field
     * @param presignedUrl
     * @return
     * 
     *  x giữ nguyên
		- y = chiều cao của trang chứa chữ ký - (toạ độ y truyền lên - pageHeight) - chiều cao ô ký
		- w = x + chiều rộng ô ký
		- h = y + chiều cao ô ký
		(pageHeight là tổng chiều cao trang thứ nhất đến trang chứa chữ ký trừ đi 1;
		ví dụ: trang chứa ô ký là trang 5 thì currentHeight sẽ là tổng chiều cao từ trang thứ nhất đến trang thứ 4
		)
     */
    private Optional<CoordinateDto> convertCoordinateToHSM(Field field, String presignedUrl) {
    	var pageIndex = field.getPage();
    	var toX = field.getCoordinateX();
    	var toY = field.getCoordinateY();
    	var toW = field.getWidth();
    	var toH = field.getHeight();
    	
    	try {
    		//chiều cao file tính từ trang 0 đến trang (pageIndex - 1)
    		var pageHeight = 0;
    				
    		var pdf = Loader.loadPDF(new URL(presignedUrl).openStream());
    		
    		for (int i = 0; i < pageIndex - 1; i++) {
                var page = pdf.getPage(i);
                pageHeight += page.getMediaBox().getHeight(); 
            }
    		
    		//chiều cao page chứa ô ký
    		var pageIndexHeight = pdf.getPage(pageIndex - 1).getMediaBox().getHeight();
            
    		//chuyển độ tọa độ và làm tròn
    		var toX1 = (int) Math.round(toX);
    		var toY1 = (int) Math.round(pageIndexHeight - (toY - pageHeight) - toH);
    		var toX2 = (int) Math.round(toX1 + toW);
    		var toY2 = (int) Math.round(toY1 + toH);
    		
    		return Optional.of(
	    				CoordinateDto.builder()
	    				.coordinateX(toX1)
	    				.coordinateY(toY1)
	    				.width(toX2)
	    				.height(toY2)
	    				.page(pageIndex)
	    				.build()
    				);
    	}catch (Exception e) {
    		log.error(String.format("can't load pdf sign hsm at \"%s\"", presignedUrl), e);
		}
    	
    	return Optional.empty();
    }

    /**
     *
     * @param field
     * @param presignedUrl
     * @return
     *
     *  x giữ nguyên
    - y = chiều cao của trang chứa chữ ký - (toạ độ y truyền lên - pageHeight) - chiều cao ô ký
    - w = chiều rộng ô ký
    - h = chiều cao ô ký
    (pageHeight là tổng chiều cao trang thứ nhất đến trang chứa chữ ký trừ đi 1;
    ví dụ: trang chứa ô ký là trang 5 thì currentHeight sẽ là tổng chiều cao từ trang thứ nhất đến trang thứ 4
    )
     */
    private Optional<CoordinateDto> convertCoordinateToPKI(Field field, String presignedUrl) {
        var pageIndex = field.getPage();
        var toX = field.getCoordinateX();
        var toY = field.getCoordinateY();
        var toW = field.getWidth();
        var toH = field.getHeight();

        try {
            //chiều cao file tính từ trang 0 đến trang (pageIndex - 1)
            var pageHeight = 0;

            var pdf = Loader.loadPDF(new URL(presignedUrl).openStream());

            for (int i = 0; i < pageIndex - 1; i++) {
                var page = pdf.getPage(i);
                pageHeight += page.getMediaBox().getHeight();
            }

            //chiều cao page chứa ô ký
            var pageIndexHeight = pdf.getPage(pageIndex - 1).getMediaBox().getHeight();

            //chuyển độ tọa độ và làm tròn
            var toX1 = (int) Math.round(toX);
            var toY1 = (int) Math.round(pageIndexHeight - (toY - pageHeight) - toH);
            var toX2 = (int) Math.round(toW);
            var toY2 = (int) Math.round(toH);

            return Optional.of(
                    CoordinateDto.builder()
                            .coordinateX(toX1)
                            .coordinateY(toY1)
                            .width(toX2)
                            .height(toY2)
                            .page(pageIndex)
                            .build()
            );
        }catch (Exception e) {
            log.error(String.format("can't load pdf sign sim pki at \"%s\"", presignedUrl), e);
        }

        return Optional.empty();
    }
}
