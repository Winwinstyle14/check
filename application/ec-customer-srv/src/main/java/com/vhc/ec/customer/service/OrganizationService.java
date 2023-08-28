package com.vhc.ec.customer.service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.customer.controller.TotalItemDto;
import com.vhc.ec.customer.dto.*;
import com.vhc.ec.customer.util.ExcelUtil;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.reflect.TypeToken;
import com.vhc.ec.customer.defination.BaseStatus;
import com.vhc.ec.customer.entity.Organization;
import com.vhc.ec.customer.exception.CustomException;
import com.vhc.ec.customer.exception.ErrorCode;
import com.vhc.ec.customer.repository.OrganizationRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;

    /**
     * Thêm mới thông tin của tổ chức
     *
     * @param orgDto {@link OrganizationDto} Thông tin của tổ chức cần tạo
     * @return {@link OrganizationDto} Thông tin của tổ chức đã được tạo thành công
     */
    @Transactional
    public OrganizationDto create(OrganizationDto orgDto) {
    	if(StringUtils.hasText(orgDto.getTaxCode())) {
    		organizationRepository.findFirstByTaxCode(orgDto.getTaxCode())
	        .ifPresent(o -> {
	            throw new CustomException(ErrorCode.TAX_CODE_IS_EXISTED);
	        });
    	}

        Optional<Organization> orgOldOptional = Optional.empty();
        if(orgDto.getParentId() != null) {
            //Lấy thông tin tổ chức cha
            orgOldOptional = organizationRepository.findById(orgDto.getParentId());
            if (orgOldOptional.isEmpty()){
                throw new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND);
            }
        }
    			
        Organization org = modelMapper.map(orgDto, Organization.class);
        org.setPath(UUID.randomUUID().toString().replaceAll("-", ""));
        org.setSmsSendMethor("API"); //Mặc định SMS gửi đến tổ chức sử dụng API send đa nhà mạng
        org.setUsbTokenVersion(orgOldOptional.get().getUsbTokenVersion());//Mặc định Usb token version = tổ chức cha gần nhất
        org.setCecaPushMode("NONE");
        Organization created = organizationRepository.save(org);

        String path = String.valueOf(created.getId());
        if (created.getParentId() != null) {
            Optional<OrganizationDto> parentOrgOptional = getById(created.getParentId() != null ? created.getParentId() : 0);
            path = parentOrgOptional.isPresent() ?
                    String.format("%s.%d", parentOrgOptional.get().getPath(), created.getId()) :
                    String.valueOf(created.getId());
        }

        created.setPath(path);
        created = organizationRepository.save(created);

        return modelMapper.map(created, OrganizationDto.class);
    }

    /**
     * Cập nhật thông tin của tổ chức
     *
     * @param id     Mã số tham chiếu tới tổ chức
     * @param orgDto Thông tin chi tiết về tổ chức
     * @return {@link OrganizationDto}
     */
    @Transactional
    public Optional<OrganizationDto> update(int id, OrganizationDto orgDto) {
    	if(StringUtils.hasText(orgDto.getTaxCode())) {
    		organizationRepository.findFirstByTaxCodeAndIdNot(orgDto.getTaxCode(), id)
            .ifPresent(o -> {
                throw new CustomException(ErrorCode.TAX_CODE_IS_EXISTED);
            });
    	} 
    	
        final var orgOptional = organizationRepository.findById(id);
        if (orgOptional.isPresent()) {
            final var org = orgOptional.get();
            org.setName(orgDto.getName());
            org.setCode(orgDto.getCode());
            org.setShortName(orgDto.getShortName());
            org.setEmail(orgDto.getEmail());
            org.setPhone(orgDto.getPhone());
            org.setFax(orgDto.getFax());
            org.setParentId(orgDto.getParentId());
            org.setTaxCode(orgDto.getTaxCode());
            org.setCecaPushMode(orgDto.getCecaPushMode());

            final var statusOptional = Arrays.stream(BaseStatus.values()).filter(
                    baseStatus -> baseStatus.ordinal() == orgDto.getStatus()
            ).findFirst();

            if (statusOptional.isPresent()) {
                org.setStatus(statusOptional.get());
            }

            // get path
            //org.setPath(null);
            org.setPath(orgDto.getPath());
            if (orgDto.getParentId() != null) {
                final var parentOptional = organizationRepository.findById(orgDto.getParentId());
                if (parentOptional.isPresent()) {
                    String path = parentOptional.get().getPath() + "." + id;
                    org.setPath(path);
                }
            }

            final var updated = organizationRepository.save(org);

            return Optional.of(
                    modelMapper.map(
                            updated,
                            OrganizationDto.class
                    )
            );
        }

        return Optional.empty();
    }

    /**
     * Lấy thông tin của tổ chức theo mã tổ chức
     *
     * @param id Mã tổ chức
     * @return Thông tin chi tiết của tổ chức
     */
    public Optional<OrganizationDto> getById(int id) {
        final var orgOptional = organizationRepository.findById(id);

        if (orgOptional.isPresent()) {
            var org = orgOptional.get();
            String rootId = (org.getPath() + ".").split("\\.")[0];
            var rootOrg = organizationRepository.findById(Integer.parseInt(rootId)).orElse(null);
            if (rootOrg != null) {
                org.setNumberOfSms(rootOrg.getNumberOfSms());
                org.setNumberOfEkyc(rootOrg.getNumberOfEkyc());
                org.setNumberOfContractsCanCreate(rootOrg.getNumberOfContractsCanCreate());
            }
        }


        return orgOptional.map(organization -> modelMapper.map(
                organization,
                OrganizationDto.class
        ));
    }

    /**
     * Lấy thông tin chi tiết của tổ chức theo khách hàng
     *
     * @param customerId Mã khách hàng
     * @return Thông tin chi tiết của tổ chức
     */
    public Optional<OrganizationDto> findByCustomerId(int customerId) {
        final var orgOptional = organizationRepository.findByCustomerId(customerId);

        return orgOptional.map(organization -> modelMapper.map(
                organization,
                OrganizationDto.class
        ));
    }

    /**
     * Tìm kiếm tổ chức trên hệ thống
     *
     * @param name     Tên tổ chức
     * @param code     Mã tổ chức
     * @param pageable Phân trang
     * @return {@link PageDto<OrganizationDto>}
     */
    public PageDto<OrganizationDto> search(int currentOrgId, String name, String code, Pageable pageable) {
        final var page = organizationRepository.search(currentOrgId, name, code, pageable);

        final var typeToken = new TypeToken<PageDto<OrganizationDto>>() {
        }.getType();

        return modelMapper.map(page, typeToken);
    }

    public Optional<OrganizationDto> findByCode(String code, int orgId) {
        final var orgOptional = organizationRepository.findByCodeOrgId(code, orgId);

        return orgOptional.map(organization -> modelMapper.map(organization, OrganizationDto.class));
    }

    public Optional<OrganizationDto> findByEmail(String email) {
        final var orgOptional = organizationRepository.findByEmail(email);

        return orgOptional.map(organization -> modelMapper.map(organization, OrganizationDto.class));
    }

    public Optional<OrganizationDto> findByName(String name, int orgId) {
        final var orgOptional = organizationRepository.findByNameOrgId(name, orgId);

        return orgOptional.map(organization -> modelMapper.map(organization, OrganizationDto.class));
    }

    public Optional<OrganizationDto> findByPhone(String phone) {
        final var orgOptional = organizationRepository.findByPhone(phone);

        return orgOptional.map(organization -> modelMapper.map(organization, OrganizationDto.class));
    }

    public List<Integer> findChildIdRecursiveById(int id) {
        return organizationRepository.findChildIdRecursiveById(id);
    }

    public AbstractTotalDto getNumberContractInPurchasedService(int id) {
        Integer number = organizationRepository.getNumberContractInPurchasedService(id);
        return new AbstractTotalDto(number == null ? 0 : number);
    }

    public void decreaseNumberOfContracts(int id) {
        log.info("decreaseNumberOfContracts {}", id);
        var rootOrg = getRootOrg(id);
        log.info("rootOrg: {}", rootOrg);
        Integer numberOfContracts = rootOrg.getNumberOfContractsCanCreate();

        if (numberOfContracts != null) {
            numberOfContracts--;
            rootOrg.setNumberOfContractsCanCreate(numberOfContracts);
            organizationRepository.save(rootOrg);
        }
    }

    public void decreaseNumberOfSms(int id) {
        log.info("decreaseNumberOfSms {}", id);
        var rootOrg = getRootOrg(id);
        Integer numberOfSms = rootOrg.getNumberOfSms();

        if (numberOfSms != null) {
            numberOfSms--;
            rootOrg.setNumberOfSms(numberOfSms);
            organizationRepository.save(rootOrg);
        }
    }

    public void decreaseNumberOfEkyc(int id) {
        log.info("decreaseNumberOfSms {}", id);
        var rootOrg = getRootOrg(id);
        Integer numberOfEkyc = rootOrg.getNumberOfEkyc();

        if (numberOfEkyc != null) {
            numberOfEkyc--;
            rootOrg.setNumberOfEkyc(numberOfEkyc);
            organizationRepository.save(rootOrg);
        }
    }

    public List<Integer> getAllOrgInTree(int id) {
        return organizationRepository.findAllOrgInTree(id);
    }

    public TotalItemDto getTotalPurchasedItem(int id) {
        var result = new TotalItemDto();
        var rootOrg = getRootOrg(id);

        if (rootOrg == null) {
            return result;
        }

        result.setContract(rootOrg.getTotalContractsPurchased() == null ? 0 : rootOrg.getTotalContractsPurchased());
        result.setSms(rootOrg.getTotalSmsPurchased() == null ? 0 : rootOrg.getTotalSmsPurchased());
        result.setEkyc(rootOrg.getTotalEkycPurchased() == null ? 0 : rootOrg.getTotalEkycPurchased());
        return result;
    }

    public TotalItemDto getUsedItem(int id) {
        var result = new TotalItemDto();
        var rootOrg = getRootOrg(id);;
        if (rootOrg == null) {
            return result;
        }

        int totalContractsPurchased = (rootOrg.getTotalContractsPurchased() == null ? 0 : rootOrg.getTotalContractsPurchased());
        int totalSmsPurchased = (rootOrg.getTotalSmsPurchased() == null ? 0 : rootOrg.getTotalSmsPurchased());
        int totalEkycPurchased = (rootOrg.getTotalEkycPurchased() == null ? 0 : rootOrg.getTotalEkycPurchased());
        int remainContracts = (rootOrg.getNumberOfContractsCanCreate() == null ? 0 : rootOrg.getNumberOfContractsCanCreate());
        int remainSms = (rootOrg.getNumberOfSms() == null ? 0 : rootOrg.getNumberOfSms());
        int remainEkyc = (rootOrg.getNumberOfEkyc() == null ? 0 : rootOrg.getNumberOfEkyc());

        result.setContract(totalContractsPurchased - remainContracts);
        result.setSms(totalSmsPurchased - remainSms);
        result.setEkyc(totalEkycPurchased - remainEkyc);
        return result;
    }

    public LicenseInfoDto getLicense(int id) {
        var rootOrg = getRootOrg(id);
        return modelMapper.map(rootOrg, LicenseInfoDto.class);
    }

    public ResponseEntity<?> importChildOrg(int currOrgId, MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        fileName = String.format("%s_%d%s",
                            fileName.substring(0, fileName.lastIndexOf('.')),
                            System.currentTimeMillis(),
                            fileName.substring(fileName.lastIndexOf('.'))
                        );

        //var file = new File("C:\\Users\\thang\\tmp\\" +  fileName);
        var file = new File(String.format("/tmp/%s", fileName));
        final int ERR_COLUMN = 6;
        try {
            multipartFile.transferTo(file);
            List<OrganizationDto> organizationList = new ArrayList<>();
            List<String> taxCodeList = new ArrayList<>();
            Organization parentOrg = null;

            try (InputStream inputStream = new FileInputStream(file)) {
                var wb = WorkbookFactory.create(inputStream);
                var sheet = wb.getSheetAt(0);
                sheet.setColumnWidth(ERR_COLUMN, 70*256);

                if (sheet.getRow(0).getLastCellNum() < 6) {
                    return ResponseEntity.ok().body(
                            MessageDto.builder()
                                    .message("File import không đúng định dạng")
                                    .success(false)
                                    .build()
                    );
                }

                var errStyle = wb.createCellStyle();
                errStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                errStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                var format = wb.createDataFormat();
                errStyle.setDataFormat(format.getFormat("@"));

                var font = wb.createFont();
                font.setColor(Font.COLOR_RED);
                var errDetailColumnStyle = wb.createCellStyle();
                errDetailColumnStyle.setFont(font);
                errDetailColumnStyle.setWrapText(true);
                StringBuilder errDetails = new StringBuilder();
                boolean error = false;
                int rowNum = 0;

                for (var row : sheet) {
                    var errCell = row.createCell(ERR_COLUMN);
                    //row.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 2);

                    if (rowNum++ == 0) {
                        errCell.setCellValue("Chi tiết lỗi");
                        continue;
                    } else {
                        errCell.setCellStyle(errDetailColumnStyle);
                    }

                    if (ExcelUtil.isRowEmpty(row)) {
                        continue;
                    }

                    var orgDto = new OrganizationDto();
                    orgDto.setEmail("");
                    orgDto.setPhone("");
                    orgDto.setStatus(1);
                    errDetails.setLength(0);

                    var orgNameCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var shortNameCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var codeCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var taxCodeCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var parentIdCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var faxCodeCell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String taxCode = ExcelUtil.getCellValue(taxCodeCell).trim();
                    String parentId = ExcelUtil.getCellValue(parentIdCell).trim();
                    orgDto.setName(ExcelUtil.getCellValue(orgNameCell).trim());
                    orgDto.setShortName(ExcelUtil.getCellValue(shortNameCell).trim());
                    orgDto.setCode(ExcelUtil.getCellValue(codeCell).trim());
                    orgDto.setTaxCode(taxCode);
                    orgDto.setFax(ExcelUtil.getCellValue(faxCodeCell).trim());

                    if (!StringUtils.hasText(orgDto.getCode())) {
                        error = true;
                        codeCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập mã tổ chức\n");
                    } else if (!orgDto.getCode().matches("[a-zA-Z0-9]+")) {
                        error = true;
                        codeCell.setCellStyle(errStyle);
                        errDetails.append("Mã tổ chức chỉ bao gồm chữ và số không chứa dấu cách, ký tự đặc biệt\n");
                    }

                    if (StringUtils.hasText(taxCode)) {
                        var org = organizationRepository.findFirstByTaxCode(taxCode).orElse(null);
                        if (org != null) {
                            error = true;
                            taxCodeCell.setCellStyle(errStyle);
                            errDetails.append("Mã số thuế đã tồn tại\n");
                        } else {
                            if (taxCodeList.contains(taxCode)) {
                                error = true;
                                taxCodeCell.setCellStyle(errStyle);
                                errDetails.append("Mã số thuế đã có trong file\n");
                            }
                        }

                        if (taxCode.length() != 10 && taxCode.length() != 14) {
                            error = true;
                            taxCodeCell.setCellStyle(errStyle);
                            errDetails.append("Mã số thuế không đúng định dạng\n");
                        }

                        taxCodeList.add(taxCode);
                    }

                    if (!StringUtils.hasText(parentId)) {
                        error = true;
                        parentIdCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập Id tổ chức cấp trên\n");
                    } else {
                        parentOrg = organizationRepository.findById(Integer.parseInt(parentId))
                                .orElse(null);
                        if (parentOrg == null) {
                            error = true;
                            parentIdCell.setCellStyle(errStyle);
                            errDetails.append("Tổ chức cấp trên không tồn tại\n");
                        } else {
                            if (parentOrg.getId() != currOrgId && !organizationRepository
                                    .findChildIdRecursiveById(currOrgId).contains(parentOrg.getId())) {

                                error = true;
                                parentIdCell.setCellStyle(errStyle);
                                errDetails.append("Tổ chức cấp trên phải thuộc tổ chức của bạn hoặc tổ chức con của bạn\n");
                            } else {
                                orgDto.setParentId(parentOrg.getId());
                            }
                        }
                    }

                    if (StringUtils.hasText(orgDto.getName())) {
                        boolean checkWithSiblings = true;
                        if (parentOrg != null && parentOrg.getName().trim().equals(orgDto.getName())) {
                            error = true;
                            orgNameCell.setCellStyle(errStyle);
                            errDetails.append("Tên tổ chức không được trùng với tên tổ chức cha\n");
                        } else {
                            for (var o : organizationList) {
                                if (o.getName() != null && orgDto.getName().equals(o.getName().trim())
                                        && orgDto.getParentId() != null
                                        && o.getParentId() != null
                                        && orgDto.getParentId() == o.getParentId()
                                ){
                                    error = true;
                                    orgNameCell.setCellStyle(errStyle);
                                    errDetails.append("Tên tổ chức không được trùng với tổ chức khác cùng cấp\n");
                                    checkWithSiblings = false;
                                    break;
                                }
                            }

                            // check voi cac to chuc trong db
                            if (checkWithSiblings && parentOrg != null) {
                                for (var o : organizationRepository.findByParentId(parentOrg.getId())) {
                                    if (orgDto.getName().equals(o.getName().trim())
                                    ){
                                        error = true;
                                        orgNameCell.setCellStyle(errStyle);
                                        errDetails.append("Tên tổ chức không được trùng với tổ chức khác cùng cấp\n");
                                        break;
                                    }
                                }
                            }
                        }

                    } else {
                        error = true;
                        orgNameCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập tên tổ chức\n");
                    }

                    errCell.setCellValue(errDetails.toString());
                    organizationList.add(orgDto);
                }// end loop row

                if (error) {
                    try (OutputStream fileOut = new FileOutputStream(file)) {
                        wb.write(fileOut);
                    }

                    var resource = new InputStreamResource(new FileInputStream(file));
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                            .contentLength(file.length())
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                }

                for (var org : organizationList) {
                    create(org);
                }

                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("error: {}", e);
        }

        return ResponseEntity.ok().body(
                MessageDto.builder()
                        .message("File import không đúng định dạng")
                        .success(false)
                        .build()
        );

    }

    private Organization getRootOrg(int id) {
        final var org = organizationRepository.findById(id).orElse(null);
        if (org == null) {
            return null;
        }

        String rootId = (org.getPath() + ".").split("\\.")[0];
        return organizationRepository.findById(Integer.parseInt(rootId)).orElse(null);
    }
}
