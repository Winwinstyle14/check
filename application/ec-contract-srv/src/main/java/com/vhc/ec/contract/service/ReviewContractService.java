package com.vhc.ec.contract.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import com.google.common.io.Files;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.repository.FieldRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewContractService {
    private final FieldRepository fieldRepository;
    private final DocumentService documentService;
    
    @Value("${vhc.ec.micro-services.gateway.api-url}")
    private String gatewayApiUrl;
    
	/**
	 * View vị trí ô ký trên file hợp đồng 
	 * @param recipientId
	 * @param base64Image
	 * @return
	 */
	
	public Optional<String> reviewSignBox(int recipientId, String base64Image) {

        var fieldOptional = fieldRepository.findByRecipientId(recipientId);

        if (fieldOptional.isPresent()) {
            var field = fieldOptional.get();
            var documentDtoCollection = documentService.findByContract(
                    field.getContractId()
            ); 

            // lấy tệp tin của hợp đồng đang được xử lý
            var docOptional = documentDtoCollection.stream()
                    .filter(documentDto -> documentDto.getType() == DocumentType.FINALLY.getDbVal())
                    .findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();

                String tempFilePath = null;
                String primaryFilePath = doc.getPath();

                // bổ sung ô ký vào file hợp đồng
                if (StringUtils.hasText(base64Image)) {
                    String fileName = UUID.randomUUID().toString();
                    var decodedBytes = Base64Utils.decodeFromString(base64Image);
                    File imageFile = new File(fileName);
                    try {
                        Files.write(decodedBytes, imageFile);
                        tempFilePath = addImage(
                                null,
                                primaryFilePath,
                                null,
                                imageFile,
                                field.getPage(),
                                field.getCoordinateX() + field.getWidth() - 140,
                                field.getCoordinateY() + field.getHeight() - 90,
                                140,
                                80
                        );
                        FileUtils.delete(imageFile);
                    } catch (IOException e) {
                        log.error("error", e);
                    }
                }
                
                return Optional.of(tempFilePath);
            }
        }

        return Optional.empty(); 
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
            var pdf = Loader.loadPDF(new URL(filePath).openStream());

            PDImageXObject image = LosslessFactory.createFromImage(pdf, ImageIO.read(imageFile)); 

            var page = pdf.getPage(pageIndex - 1);
            var pageHeight = page.getMediaBox().getHeight();

            ty = ty - ((pageIndex - 1) * pageHeight);

            var contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true);

            // vẽ hình ảnh vào vị trí cụ thể trên trang tài liệu
            contentStream.drawImage(image, tx, (pageHeight - ty) - height, width, height);
            contentStream.close();
            
            try {
	    		java.nio.file.Files.createDirectories(Paths.get("./tmp/file/")); 
	        } catch (IOException e) {
	             log.error("can't create directory {}", "./tmp/file/", e);
	        }
            
            final var fileName = String.format("%s.pdf", UUID.randomUUID().toString());
            
            final var tempFilePath = String.format("./tmp/file/%s", fileName);  
            
            //final var tempFilePath = String.format("D:\\workspace2\\econtract-service\\application\\ec-file-srv\\tmp\\file/%s", fileName);  
             
            pdf.save(tempFilePath);
            pdf.close();
            
            return String.format("%s/tmp/%s", gatewayApiUrl, fileName);
        } catch (IOException e) {
            log.error(String.format("can't load pdf at \"%s\" or can't load image at \"%s\"", filePath, imagePath), e);
        } catch (Exception e) {
            log.error(String.format("can't draw image \"%s\" on pdf doc \"%s\"", imagePath, filePath), e);
        }

        return null;
    }
}
