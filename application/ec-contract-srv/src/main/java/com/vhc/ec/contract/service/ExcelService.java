package com.vhc.ec.contract.service;

import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.dto.DocumentDto;
import com.vhc.ec.contract.dto.ParticipantDto;
import com.vhc.ec.contract.dto.RecipientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Xử lý tệp tin tạo hợp đồng theo lô
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Deprecated(forRemoval = true)
public class ExcelService {

    private final ContractService contractService;
    private final DocumentService documentService;
    private final FieldService fieldService;
    private final FileService fileService;
    private final ParticipantService participantService;

    /**
     * Tự động tạo tệp tin cho phép người dùng nhập thông tin của hợp đồng theo lô
     *
     * @param contractId Mã tham chiếu tới hợp đồng
     * @return Thông tin tệp tin được lưu trữ trên hệ thống
     */
    public Optional<DocumentDto> generateBatchFile(int contractId) {
        final var contractOptional = contractService.findById(contractId);

        //
        if (contractOptional.isPresent()) {
            var rowIndex = 0;
            var colIndex = 0;
            final var contract = contractOptional.get();

            final Workbook workbook = new XSSFWorkbook();
            final var sheet = workbook.createSheet("data");
            var row = sheet.createRow(rowIndex);
            rowIndex = rowIndex + 1;

            final var dateFormat = workbook.getCreationHelper()
                    .createDataFormat().getFormat("dd/mm/yyyy");
            final var dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(dateFormat);

            //
            row.createCell(colIndex, CellType.STRING).setCellValue("Tên hợp đồng");
            colIndex = colIndex + 1;

            row.createCell(colIndex, CellType.STRING).setCellValue("Số hợp đồng");
            colIndex = colIndex + 1;

            var cell = row.createCell(colIndex);
            cell.setCellValue("Ngày ký");
            cell.setCellStyle(dateStyle);
            colIndex = colIndex + 1;

            // lấy thông tin của hợp đồng (thông tin không thay đổi)
            final var contractName = contract.getName();
            final var contractNo = contract.getContractNo();
            final var contractSignTime = contract.getSignTime();

            // lấy thông tin tổ chức
            var participantCollection = contract.getParticipants().stream()
                    .sorted(Comparator.comparingInt(ParticipantDto::getType))
                    .sorted(Comparator.comparingInt(ParticipantDto::getOrdering))
                    .collect(Collectors.toList());

            for (var index = 0; index < participantCollection.size(); index++) {
                cell = row.createCell(colIndex, CellType.STRING);
                cell.setCellValue(String.format("Tên tổ chức %d", index + 1));
                colIndex = colIndex + 1;

                final var prefixColHeader = String.format("Tổ chức %d. ", (index + 1));

                final var recipientCollection = participantCollection
                        .get(index).getRecipients().stream()
                        .sorted(Comparator.comparingInt(RecipientDto::getRole))
                        .sorted(Comparator.comparingInt(RecipientDto::getOrdering))
                        .collect(Collectors.toList());

                for (var recipientDto : recipientCollection) {
                    var recipientRoleOptional = Arrays.stream(RecipientRole.values())
                            .filter(recipientRole -> recipientRole.getDbVal() == recipientDto.getRole())
                            .findFirst();

                    String name = null;
                    String email = null;

                    if (recipientRoleOptional.isPresent()) {
                        switch (recipientRoleOptional.get()) {
                            case COORDINATOR:
                                name = "Tên người điều phối";
                                email = "Email người điều phối";
                                break;
                            case REVIEWER:
                                name = "Tên người xem xét";
                                email = "Email người xem xét";
                                break;
                            case SIGNER:
                                name = "Tên người ký";
                                email = "Email người ký";
                                break;
                            case ARCHIVER:
                                name = "Tên văn thư";
                                email = "Email văn thư";
                                break;
                            default:
                                return Optional.empty();
                        }
                    }

                    // tên người xử lý hồ sơ
                    cell = row.createCell(colIndex, CellType.STRING);
                    cell.setCellValue(prefixColHeader + name);
                    colIndex = colIndex + 1;

                    // địa chỉ email của người xử lý hồ sơ
                    cell = row.createCell(colIndex, CellType.STRING);
                    cell.setCellValue(prefixColHeader + email);
                    colIndex = colIndex + 1;
                }
            }

            // trường dữ liệu đã được định nghĩa trong hợp đồng mẫu
            final var fieldCollection = fieldService.findByContract(contractId)
                    .stream().filter(fieldDto -> fieldDto.getType() == FieldType.TEXT.getDbVal())
                    .collect(Collectors.toList());

            for (var field : fieldCollection) {
                cell = row.createCell(colIndex);
                cell.setCellValue(field.getName() != null ? field.getName() : "");
                colIndex = colIndex + 1;
            }

            // nhập dữ liệu mẫu
            row = sheet.createRow(rowIndex);

            // reset col index
            colIndex = 0;

            // begin fill sample value
            cell = row.createCell(colIndex);
            cell.setCellValue(contractName);
            colIndex = colIndex + 1;

            //
            cell = row.createCell(colIndex);
            cell.setCellValue(contractNo);
            colIndex = colIndex + 1;

            //
            cell = row.createCell(colIndex);
            cell.setCellValue(contractSignTime);
            cell.setCellStyle(dateStyle);
            colIndex = colIndex + 1;

            // thành phần tham gia vào quá trình xử lý hồ sơ
            for (ParticipantDto participantDto : participantCollection) {
                cell = row.createCell(colIndex);
                cell.setCellValue(participantCollection.get(0).getName());
                colIndex = colIndex + 1;

                final var recipientCollection = participantDto.getRecipients().stream()
                        .sorted(Comparator.comparingInt(RecipientDto::getRole))
                        .sorted(Comparator.comparingInt(RecipientDto::getOrdering))
                        .collect(Collectors.toList());

                for (var recipientDto : recipientCollection) {
                    cell = row.createCell(colIndex);
                    cell.setCellValue(recipientDto.getName());
                    colIndex = colIndex + 1;

                    cell = row.createCell(colIndex);
                    cell.setCellValue(recipientDto.getEmail());
                    colIndex = colIndex + 1;
                }
            }

            // lưu tệp tin hợp đồng mẫu theo lô lên hệ thống lưu trữ tệp tin
            final String exportFileName = String.format("./tmp/%s.xlsx", UUID.randomUUID());

            try (var fos = new FileOutputStream(exportFileName)) {
                workbook.write(fos);

                //
                final var documentDtoOptional = documentService.findByContract(contractId)
                        .stream().filter(documentDto -> documentDto.getType() == DocumentType.PRIMARY.getDbVal())
                        .findFirst();

                if (documentDtoOptional.isPresent()) {
                    final var documentDto = documentDtoOptional.get();
                    final var bucket = documentDto.getBucket();

                    final var uploadedOptional = fileService.replace(exportFileName, bucket, null);
                    if (uploadedOptional.isPresent()) {
                        final var uploaded = uploadedOptional.get();
                        // lưu thông tin của tệp tin mẫu
                        documentDto.setId(0);
                        documentDto.setFilename(uploaded.getFileObject().getFilename());
                        documentDto.setPath(uploaded.getFileObject().getFilePath());
                        documentDto.setType(DocumentType.BATCH.getDbVal());

                        final var created = documentService.create(documentDto);

                        return Optional.of(created);
                    }
                }
            } catch (IOException e) {
                log.error("can't not save excel file", e);
            }
        }

        return Optional.empty();
    }

    public void process(int contractId, String filePath) {
        try {
            final var workbook = new XSSFWorkbook(filePath);
            final var sheet = workbook.getSheetAt(0);
            for (var row : sheet) {
                // bỏ qua tiêu đề của mỗi sheet
                if (row.getRowNum() > 0) {
                    //
                }
            }

        } catch (IOException e) {
            log.error("file \"{}\" not found.", filePath, e);
        }
    }

    /**
     * Kiểm tra nội dung tệp tin hợp đồng theo lô được khách hàng tải lên
     *
     * @param contractId Mã số tham chiếu tới hợp đồng mẫu
     * @param filePath   Đường dẫn tới tệp tin khách hàng tải lên
     * @return Danh sách chi tiết lỗi
     */
    public List<String> validate(int contractId, String filePath) {
        final var messageList = new ArrayList<String>();

        try {
            final var workbook = new XSSFWorkbook(filePath);
            final var sheet = workbook.getSheetAt(0);

            for (var row : sheet) {
                // kiểm tra số lượng cột dữ liệu cần nhập
                if (row.getRowNum() == 0) {
                    int totalColumn = 3;
                    totalColumn = totalColumn + participantService.findByContract(contractId).stream()
                            .mapToInt(value -> (value.getRecipients().size() + 1))
                            .sum();

                    if (totalColumn != (row.getLastCellNum() + 1)) {
                        messageList.add("Dữ liệu tải lên không hợp lệ");
                    }
                }

                // kiểm tra dữ liệu của người dùng nhập vào
                if (row.getRowNum() > 0) {
                    final var cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        final var cell = cellIterator.next();

                        // kiểm tra dữ liệu để trống
                        if (cell.getCellType() == CellType.BLANK
                                && cell.getColumnIndex() != 1 && cell.getColumnIndex() != 2) {
                            messageList.add(
                                    String.format(
                                            "Cột %d, dòng %d không được để trống",
                                            cell.getRowIndex(),
                                            cell.getColumnIndex()
                                    )
                            );
                        }

                        // kiểm tra trường dữ liệu thời gian
                        if (cell.getColumnIndex() == 2 && cell.getCellType() != CellType.BLANK && !DateUtil.isCellDateFormatted(cell)) {
                            messageList.add(
                                    String.format(
                                            "Cột %d, dòng %d không đúng định dạng ngày tháng",
                                            cell.getRowIndex(),
                                            cell.getColumnIndex()
                                    )
                            );
                        }
                    }
                }
            }

            FileUtils.delete(new File(filePath));
        } catch (IOException e) {
            log.error("file \"{}\" not found", filePath, e);
        }

        return messageList;
    }
}
