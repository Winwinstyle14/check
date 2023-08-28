package com.vhc.ec.filemgmt.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;
import com.vhc.ec.filemgmt.dto.ComposeRequest;
import com.vhc.ec.filemgmt.dto.UploadFileResponse;
import com.vhc.ec.filemgmt.util.EncryptorAes;
import com.vhc.ec.filemgmt.util.StringUtil;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    @Value("${vhc.ec.minio.expires}")
    private int expries; 
    @Value("${vhc.ec.micro-services.gateway.api-url}")
    private String gatewayApiUrl;

    private final BucketService bucketService;
    private final MinioClient client;

    /**
     * Tải tệp tin mới lên hệ thống MinIO
     *
     * @param multipartFile {@link MultipartFile} Nội dung tệp tin cần lưu trữ
     * @param bucket        Bucket được hệ thống tạo cho tổ chức
     * @return Đường dẫn lưu trữ của tệp tin
     */
    public String upload(MultipartFile multipartFile, String bucket) {
        return createOrReplace(multipartFile, bucket, null);
    }

    /**
     * Tải tệp tin mới lên hệ thống lưu trữ, hỗ trợ chuỗi Base64
     *
     * @param bucket      Bucket được hệ thống tạo cho tổ chức
     * @param inputStream Nội dung tệp tin
     * @param fileName    Tên tệp tin
     * @param length      Độ dài của tệp tin
     * @param mimeType    Loại nội dung
     * @return Đường dẫn tới tệp tin, đã được lưu trữ trên hệ thống
     */
    public String upload(String bucket, final InputStream inputStream, String fileName, long length, String mimeType) {
        // Kiểm tra nếu bucket chưa được tạo thì tạo mới
        boolean isSuccess = bucketService.createBucketIfNotExists(bucket);
        if (!isSuccess) return null;

        Date now = new Date();
        String dateString = sdf.format(now);

        final var filePath = String.format("%s/%s",
                dateString,
                fileName
        );

        try {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .stream(
                                    inputStream,
                                    length,
                                    -1
                            ).contentType(mimeType)
                            .build()
            );
        } catch (Exception e) {
            log.error("can't upload base64 string.", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("can't close input stream", e);
                }
            }
        }

        return filePath;
    }

    /**
     * Ghi đè tập tin lên hệ thống lưu trữ
     *
     * @param multipartFile Thông tin chi tiết của tệp tin cần lưu
     * @param bucket        Bucket được hệ thống tạo cho tổ chức
     * @param filePath      Đường dẫn lưu trữ của tệp tin trên hệ thống lưu trữ
     * @return Đường dẫn lưu trữ của tệp tin
     */
    public String update(MultipartFile multipartFile, String bucket, String filePath) {
        return createOrReplace(multipartFile, bucket, filePath);
    }

    /**
     * Kết hợp nhiều tệp tin, thành tệp tin dạng nén
     *
     * @param composeRequest Thông tin chi tiết của tệp tin
     * @return Đường dẫn tới tệp tin
     */
    public UploadFileResponse compose(ComposeRequest composeRequest) {
        log.error("COMPOSE = {}", composeRequest.toString());

        var uploaded = UploadFileResponse.builder()
                .success(false)
                .message("failure")
                .build();

        // Kiểm tra nếu bucket chưa được tạo thì tạo mới
        var isSuccess = bucketService.createBucketIfNotExists(composeRequest.getBucket());
        if (!isSuccess) return uploaded;

        var now = new Date();
        var dateString = sdf.format(now);

        final var tempDir = UUID.randomUUID().toString();
        final var listFileDir = String.format("%s/%s", tempDir, composeRequest.getFolderName());
        try {
            java.nio.file.Files.createDirectories(Paths.get("/tmp/" + listFileDir));
            java.nio.file.Files.createDirectories(Paths.get("/tmp/" + listFileDir + "/contract"));
            java.nio.file.Files.createDirectories(Paths.get("/tmp/" + listFileDir + "/attachFile"));

        } catch (IOException e) {
            log.error("can't create directory {}", listFileDir, e);
        }

        // Download file hợp đồng
        composeRequest.getFileRequestCollection().forEach(fileRequest -> {
            try {
                var inputStream = client.getObject(
                        GetObjectArgs.builder()
                                .bucket(fileRequest.getBucket())
                                .object(fileRequest.getPath())
                                .build()
                );


                // download url
                byte[] encrypBytes = FileCopyUtils.copyToByteArray(inputStream);
                
                // Giải mã file hợp đồng
                byte[] decrypBytes = EncryptorAes.decryptFile(encrypBytes);

                if (decrypBytes != null) {
                    final var temFilePath = String.format(
                            "/tmp/%s/contract/%s", listFileDir, fileRequest.getFileName()
                    );
                    Files.write(decrypBytes, new File(temFilePath));
                }
            } catch (Exception e) {
                log.error("can't get file {}", fileRequest.toString(), e);
            }
        });
        
        // Download file đính kèm và hợp đồng liên quan
        composeRequest.getAttachFileRequestCollection().forEach(fileRequest -> {
            try {
                var inputStream = client.getObject(
                        GetObjectArgs.builder()
                                .bucket(fileRequest.getBucket())
                                .object(fileRequest.getPath())
                                .build()
                );
 
                // download url
                byte[] encrypBytes = FileCopyUtils.copyToByteArray(inputStream);
                
                // Giải mã file hợp đồng
                byte[] decrypBytes = EncryptorAes.decryptFile(encrypBytes);

                if (decrypBytes != null) {
                    final var temFilePath = String.format(
                            "/tmp/%s/attachFile/%s", listFileDir, fileRequest.getFileName()
                    );
                    Files.write(decrypBytes, new File(temFilePath));
                }
            } catch (Exception e) {
                log.error("can't get file {}", fileRequest.toString(), e);
            }
        });

        try {
        	//File nén trước khi mã hóa
            String zipFileName = String.format("/tmp/%s/%s.zip", tempDir, UUID.randomUUID());
            zipDirectory("/tmp/" + listFileDir, zipFileName);
            
            //Đọc file zip
            FileInputStream fis = new FileInputStream(zipFileName);
            
            //Mã hóa file trước khi đẩy lên MinIO
      		var encryptedText =  EncryptorAes.encryptFile(fis.readAllBytes());
              
      		//Chuyển đổi dữ liệu mã hóa sang inputstream
      		//final var inputStream = new ByteArrayInputStream(encryptedText);
      		
      		//File nén sau khi mã hóa
      		String zipFile = String.format("/tmp/%s/%s.zip", tempDir, UUID.randomUUID());
      		Path path = Paths.get(zipFile);
      		java.nio.file.Files.write(path, encryptedText);
            
            //Đường dẫn thư mục trên MinIO
            final var filePath = String.format("%s/%s.zip",
                    dateString,
                    UUID.randomUUID()
            );

            // upload zip file to server
            try {
                client.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(composeRequest.getBucket())
                                .object(filePath)
                                .filename(zipFile)
                                .contentType("application/zip")
                                .build()
                );

                uploaded = UploadFileResponse.builder()
                        .success(true)
                        .message("successful")
                        .fileObject(
                                UploadFileResponse.Uploaded.builder()
                                        .bucket(composeRequest.getBucket())
                                        .filePath(filePath)
                                        .build()
                        )
                        .build();
            } catch (Exception e) {
                log.error("can't upload file \"{}\" to minio server", zipFileName, e);
            }
        } catch (IOException e) {
            log.error("can't zip directory {}", tempDir, e);
        } finally {
            try {
                FileSystemUtils.deleteRecursively(Path.of(tempDir));
            } catch (IOException e) {
                log.error("can't delete dierctory {}", tempDir, e);
            }
        }

        return uploaded;
    }

    /**
     * Lấy đường dẫn tạm của tệp tin, hiển thị cho người dùng cuối
     *
     * @param bucket   Bucket được hệ thống tạo cho tổ chức
     * @param filePath Đường dẫn tới tệp tin
     * @return Đường dẫn tạm của tệp tin
     */
    public String getPresignedObjectUrl(String bucket, String filePath) {
        // lấy đường dẫn tạm trên hệ thống lưu trữ nội dung
        try {
        	String linkMinIO = client.getPresignedObjectUrl(
	                    GetPresignedObjectUrlArgs.builder()
	                    .method(Method.GET)
	                    .bucket(bucket)
	                    .object(filePath)
	                    .expiry(expries * 60 * 60)
	                    .build()
        			);
        	
        	//Lấy link view file đã giải mã
            if(linkMinIO != null) {
            	//Lấy file từ link url MinIO
        		URL urlMinIO = new URL(linkMinIO);
        		BufferedInputStream inputStream = new BufferedInputStream(urlMinIO.openStream()); 
            	byte[] byteEncryp = FileCopyUtils.copyToByteArray(inputStream);
            	
            	//Giải mã file lấy được từ MinIO
            	byte[] byteDecryp = EncryptorAes.decryptFile(byteEncryp);
            	
            	//Tạo thư mục và lưu file tạm thời
            	final var tempDir = UUID.randomUUID().toString();
            	
            	//Lấy tên file từ url
            	final var fileName = Paths.get(urlMinIO.getPath()).getFileName().toString();
            	
            	//Tạo thư mục /tmp
    	    	try {
    	    		java.nio.file.Files.createDirectories(Paths.get("./tmp/file/")); 
    	        } catch (IOException e) {
    	             log.error("can't create directory {}", tempDir, e);
    	        }  

                if (byteDecryp != null) {
                    final var temFilePath = String.format("./tmp/file/%s", fileName);
                    Files.write(byteDecryp, new File(temFilePath));
                    
                    return String.format("%s/tmp/%s", gatewayApiUrl, fileName);
                }
            }
        } catch (Exception e) {
            log.error(
                    String.format(
                            "can't get presigned object url. {\"bucket\": \"%s\", \"filePath\": \"%s\"}",
                            bucket, filePath
                    ), e
            );
        }

        return null;
    }

    /**
     * copy object in minio
     * @param sourceBucket source bucket
     * @param sourceFilePath source file path
     * @return new file path
     */
    public String copyObject(String sourceBucket, String sourceFilePath) {
        var extensionName = Files.getFileExtension(sourceFilePath);

        var dateString = sdf.format(new Date());
        var newFilePath = String.format("%s/%s.%s", dateString, UUID.randomUUID(), extensionName);

        var copyObjectArgs = CopyObjectArgs.builder()
                .bucket(sourceBucket)
                .object(newFilePath)
                .source(
                        CopySource.builder()
                                .bucket(sourceBucket)
                                .object(sourceFilePath)
                                .build()
                )
                .build();

        try {
            client.copyObject(copyObjectArgs);
            return newFilePath;
        } catch (Exception e) {
            log.error("can't copy object, bucket = {}, filePath = {}", sourceBucket, sourceFilePath, e);
        }

        return null;
    }
    
    /**
     * Lấy file định dạng Hex từ MinIO
     * @param bucket
     * @param filePath
     * @return
     */
    public String getHexToFileMinIO(String bucket, String filePath) {
    	try {
    		String linkMinIO = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(filePath)
                    .expiry(expries * 60 * 60)
                    .build()
    			);
    	
	        if(linkMinIO != null) {
	        	//Lấy file từ link url MinIO
	    		URL urlMinIO = new URL(linkMinIO);
	    		BufferedInputStream inputStream = new BufferedInputStream(urlMinIO.openStream()); 
	    		
	        	byte[] byteEncryp = FileCopyUtils.copyToByteArray(inputStream); 
	        	
                if (byteEncryp != null) {
                	//Giải mã file lấy được từ MinIO
    	        	byte[] byteDecryp = EncryptorAes.decryptFile(byteEncryp); 
    	        	
                    byte[] encodedBytes = Base64.getEncoder().encode(byteDecryp);
    	        	
    	        	//Convert bytes to hex
    	    		String hexEncodedBytes = StringUtil.bytesToHex(encodedBytes);
    	    		
    	    		return hexEncodedBytes;
                }
	        }
		} catch (Exception e) {
			log.error(
                    String.format(
                            "can't get getHexToFileMinIO. {\"bucket\": \"%s\", \"filePath\": \"%s\"}",
                            bucket, filePath
                    ), e
            );
		}
    	
    	return null;
    }

    /**
     * Tải tệp tin lên hệ thống lưu trữ:
     * - Nếu tệp tin chưa tồn tại thì tạo mới
     * - Nếu tệp tin đã tồn tại thì thay thế
     *
     * @param multipartFile Thông tin của tệp tin cần gửi đi
     * @param bucket        Bucket được hệ thống tạo cho tổ chức
     * @param filePath      Đường dẫn lưu trữ trên hệ thống MinIO
     * @return Đường dẫn lưu trữ của tệp tin
     */
    private String createOrReplace(MultipartFile multipartFile, String bucket, String filePath) {
        // Kiểm tra nếu bucket chưa được tạo thì tạo mới
        boolean isSuccess = bucketService.createBucketIfNotExists(bucket);
        if (!isSuccess) return null;

        if (!StringUtils.hasText(filePath)) {
            Date now = new Date();
            String dateString = sdf.format(now);

            filePath = String.format("%s/%s.%s",
                    dateString,
                    UUID.randomUUID(),
                    Files.getFileExtension(
                            multipartFile.getOriginalFilename()
                    )
            );
        }

        try {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .stream(
                                    multipartFile.getInputStream(),
                                    multipartFile.getSize(),
                                    -1
                            )
                            .contentType(
                                    multipartFile.getContentType())
                            .build()
            );

            return filePath;
        } catch (IOException e) {
            log.error("can't get input stream.", e);
        } catch (Exception e) {
            log.error("can't upload file to minio.", e);
        }

        return null;
    }

    private void zipDirectory(String sourceDir, String compressFile) throws IOException {
        var fos = new FileOutputStream(compressFile);
        var zipOut = new ZipOutputStream(fos);
        var fileToZip = new File(sourceDir);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip); 
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
