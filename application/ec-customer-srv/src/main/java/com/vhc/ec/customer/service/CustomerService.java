package com.vhc.ec.customer.service;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.customer.defination.BaseStatus;
import com.vhc.ec.customer.dto.*;
import com.vhc.ec.customer.entity.Customer;
import com.vhc.ec.customer.repository.CustomerRepository;
import com.vhc.ec.customer.repository.OrganizationRepository;
import com.vhc.ec.customer.repository.RoleRepository;
import com.vhc.ec.customer.util.ExcelUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    /**
     * Thêm mới khách hàng
     *
     * @param customerDto Thông tin chi tiết của khách hàng cần thêm
     * @return Thông tin của khách hàng đã được thêm trên hệ thống
     */
    public CustomerDto create(CustomerDto customerDto) {
        final var customer = modelMapper.map(customerDto, Customer.class);

        var password = customer.getPassword();
        if (!StringUtils.hasText(password)) {
            // generate password
            password = RandomStringUtils.randomAlphabetic(6);
        }

        // using bcrypt hash password
        String bcryptPassword = passwordEncoder.encode(password);
        customer.setPassword(bcryptPassword);

        final var created = customerRepository.save(customer);

        AccountNoticeRequest accountNoticeRequest;

        accountNoticeRequest = AccountNoticeRequest
                .builder()
                .email(customer.getEmail())
                .name(customer.getName())
                .accessCode(password)
                .phone(customer.getPhone())
                .build();

        // sending email notice
        try {
            restTemplate.postForObject("http://ec-notification-srv/api/v1/internal/notification/customerAccountNotice", accountNoticeRequest, Object.class);
        } catch (Exception e) {
            log.error("error", e);
        }

        return modelMapper.map(created, CustomerDto.class);
    }

    /**
     * Cập nhật thông tin của khách hàng
     *
     * @param id          Mã số tham chiếu tới khách hàng
     * @param customerDto Thông tin chi tiết của khách hàng
     * @return Thông tin chi tiết của khách hàng
     */
    public Optional<CustomerDto> update(int id, CustomerDto customerDto) {
        final var customerOptional = customerRepository.findById(id);
        if (customerOptional.isPresent()) {
            final var customer = customerOptional.get();
            var org = organizationRepository
                    .findById(customerDto.getOrganizationId()).orElse(null);
            if (org != null) {
                customer.setOrganization(org);
                customer.setOrganizationId(customerDto.getOrganizationId());
            } else {
                log.warn("Cannot find organization has id: {}", customer.getOrganizationId());
            }
            // cập nhật thông tin khách hàng
            customer.setName(customerDto.getName());
            customer.setPhone(customerDto.getPhone());
            customer.setPhoneSign(customerDto.getPhoneSign());
            customer.setPhoneTel(customerDto.getPhoneTel());
            customer.setBirthday(customerDto.getBirthday());
            customer.setRoleId(customerDto.getRoleId());
            customer.setHsmName(customerDto.getHsmName());
            customer.setHsmPass(customerDto.getHsmPass());
            customer.setTaxCode(customerDto.getTaxCode());
            customer.setStatus(
                    customerDto.getStatus() == 0 ? BaseStatus.IN_ACTIVE : BaseStatus.ACTIVE
            );
            final var signImageList = new ArrayList<Map<String, String>>();
            customerDto.getSignImage().forEach(signImage -> {
                final var map = new HashMap<String, String>();
                map.put("path", signImage.getPath());
                map.put("bucket", signImage.getBucket());

                signImageList.add(map);
            });
            customer.setSignImage(signImageList);
            
            //Bổ sung trạng thái chuyển tổ chức
            customer.setOrganizationChange(customerDto.getOrganizationChange());

            final var updated = customerRepository.save(customer);
            return Optional.of(
                    modelMapper.map(updated, CustomerDto.class)
            );
        }

        return Optional.empty();
    }

    /**
     * Lấy thông tin chi tiết của khách hàng
     *
     * @param id Mã số tham chiếu của khách hàng
     * @return Thông tin của khách hàng
     */
    public Optional<CustomerDto> getById(int id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);

        return customerOptional.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    /**
     * Lấy thông tin của khách hàng theo địa chỉ email
     *
     * @param email Địa chỉ Email
     * @return {@link Optional<CustomerDto>} Thông tin của khách hàng nếu tồn tại
     * @see CustomerDto
     */
    public Optional<CustomerDto> getByEmail(String email) {
        return customerRepository.findTopByEmail(email)
                .map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    /**
     * Danh sách khách hàng thuộc tổ chức, theo mã tham chiếu của tổ chức
     *
     * @param orgId Mã tham chiếu của tổ chức
     * @return Danh sách khách hàng của tổ chức
     */
    public Collection<CustomerDto> getByOrganizationId(int orgId) {
        final var customerCollection = customerRepository.findByOrganizationIdOrderByNameAsc(orgId);

        return modelMapper.map(
                customerCollection,
                new TypeToken<Collection<CustomerDto>>() {
                }.getType()
        );
    }

    /**
     * Cập nhật mật khẩu của khách hàng
     *
     * @param email    Địa chỉ email của khách hàng
     * @param password Mật khẩu đăng nhập mới của khách hàng
     * @return Trạng thái cập nhật mậu khẩu
     */
    public boolean updatePassword(String email, String password) {

        final var customerOptional = customerRepository.findTopByEmail(email);

        if (customerOptional.isEmpty())
            return false;

        final var customer = customerOptional.get();
        customer.setPassword(passwordEncoder.encode(password));
        customerRepository.save(customer);

        return true;
    }

    /**
     * Cập nhật mật khẩu của khách hàng
     *
     * @param id          Mã số tham chiếu của khách hàng
     * @param password    Mật khẩu cũ của khách hàng
     * @param newPassword Mật khẩu mới của khách hàng
     * @return Trạng thái cập nhật mật khẩu
     */
    public boolean updatePassword(Integer id, String password, String newPassword) {

        final var customerOptional = customerRepository.findById(id);

        if (customerOptional.isEmpty())
            return false;

        final var customer = customerOptional.get();
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            return false;
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        return true;
    }

    /**
     * Tìm kiếm thông tin của khách hàng
     *
     * @param name     Họ và tên khách hàng
     * @param email    Địa chỉ email của khách hàng
     * @param phone    Số điện thoại
     * @param orgId    Mã số tham chiếu tới tổ chức
     * @param pageable Thông tin phân trang
     * @return Danh sách khách hàng (hỗ trợ phân trang)
     */
    public PageDto<CustomerDto> search(int currentUserId, String name, String email, String phone, Integer orgId, Pageable pageable) {
        int currOrgId = 0;

        final var customerOptional = customerRepository.findById(currentUserId);
        if (customerOptional.isPresent()) {
            currOrgId = customerOptional.get().getOrganizationId();
        }

        final var page = customerRepository.search(name, email, phone, orgId, currOrgId, pageable);
        final var typeToken = new com.google.common.reflect.TypeToken<PageDto<CustomerDto>>() {
        }.getType();

        return modelMapper.map(page, typeToken);
    }

    public Optional<CustomerDto> findByPhone(String phoneTel) {
        final var customerOptional = customerRepository.findFirstByPhone(phoneTel);

        return customerOptional.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    public ServiceStatusRes checkServiceStatus(CustomerUser user) {
        final var customer = customerRepository.findById(user.getId()).orElse(null);
        if (customer != null) {
            int orgId = customer.getOrganizationId();
            var org = organizationRepository.findById(orgId).orElse(null);
            if (org != null) {
                String rootId = (org.getPath() + ".").split("\\.")[0];
                var rootOrg = organizationRepository.findById(Integer.valueOf(rootId)).orElse(null);
                if (rootOrg != null) {
                    if (rootOrg.getStatus() == BaseStatus.PENDING) {
                        return new ServiceStatusRes("Pending");
                    }

                    var now = LocalDate.now();
                    Integer numberOfContractsCanCreate = rootOrg.getNumberOfContractsCanCreate();
                    numberOfContractsCanCreate = (numberOfContractsCanCreate == null ? 0 : numberOfContractsCanCreate);
                    var startLicense = rootOrg.getStartLicense();
                    var endLicense = rootOrg.getEndLicense();

                    if (startLicense != null && endLicense != null
                            && (now.isBefore(startLicense) || now.isAfter(endLicense))
                    ) {
                        return new ServiceStatusRes("End");
                    }

                    if (numberOfContractsCanCreate <= 0) {
                        return new ServiceStatusRes("End");
                    }

                    return new ServiceStatusRes("Using");
                }
            }
        }

        return new ServiceStatusRes();
    }

    public ResponseEntity<?> importUsers(int organizationId, MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        fileName = String.format("%s_%d%s",
                fileName.substring(0, fileName.lastIndexOf('.')),
                System.currentTimeMillis(),
                fileName.substring(fileName.lastIndexOf('.'))
        );

        //var file = new File("C:\\Users\\thang\\tmp\\" +  fileName);
        var file = new File(String.format("/tmp/%s", fileName));
        final int ERR_COLUMN = 6;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            multipartFile.transferTo(file);
            List<CustomerDto> customerList = new ArrayList<>();

            try (InputStream inputStream = new FileInputStream(file)) {
                var wb = WorkbookFactory.create(inputStream);
                var sheet = wb.getSheetAt(0);
                if (sheet.getRow(0).getLastCellNum() < 6) {
                    return ResponseEntity.ok().body(
                            MessageDto.builder()
                                    .message("File import không đúng định dạng")
                                    .success(false)
                                    .build()
                    );
                }

                sheet.setColumnWidth(ERR_COLUMN, 50*256);
                var errStyle = wb.createCellStyle();
                errStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                errStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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

                    if (rowNum++ == 0) {
                        errCell.setCellValue("Chi tiết lỗi");
                        continue;
                    } else {
                        errCell.setCellStyle(errDetailColumnStyle);
                    }

                    if (ExcelUtil.isRowEmpty(row)) {
                        continue;
                    }

                    var customerDto = new CustomerDto();
                    customerDto.setStatus(1);
                    errDetails.setLength(0);
                    var nameCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var emailCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var birthdayCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String birthdayStr = ExcelUtil.getCellValue(birthdayCell);
                    var phoneCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var orgIdCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String orgInputId = ExcelUtil.getCellValue(orgIdCell);

                    var roleCodeCell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String roleCode = roleCodeCell.getRichStringCellValue().getString();

                    customerDto.setName(nameCell.getRichStringCellValue().getString());
                    customerDto.setEmail(emailCell.getRichStringCellValue().getString());
                    String phone = ExcelUtil.getCellValue(phoneCell).trim();
                    //phone = phone.startsWith("0") ? phone : "0" + phone ;
                    customerDto.setPhone(phone);
                    customerDto.setStatus(1);
                    customerDto.setHsmName("");
                    customerDto.setHsmPass("");
                    customerDto.setTaxCode("");
                    customerDto.setSignImage(Collections.emptyList());

                    if (!StringUtils.hasText(customerDto.getName())) {
                        error = true;
                        nameCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập tên tổ chức\n");
                    }

                    if (!StringUtils.hasText(customerDto.getEmail())) {
                        error = true;
                        emailCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập email\n");
                    } else if (customerRepository.findTopByEmail(customerDto.getEmail()).isPresent()) {
                        error = true;
                        emailCell.setCellStyle(errStyle);
                        errDetails.append("Email đã có trong hệ thống\n");
                    } else if (!EmailValidator.getInstance().isValid(customerDto.getEmail())) {
                        error = true;
                        emailCell.setCellStyle(errStyle);
                        errDetails.append("Email không đúng định dạng\n");
                    }

                    if (!StringUtils.hasText(customerDto.getPhone())) {
                        error = true;
                        phoneCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập SĐT\n");
                    } else if (customerRepository.findFirstByPhone(customerDto.getPhone()).isPresent()) {
                        error = true;
                        phoneCell.setCellStyle(errStyle);
                        errDetails.append("SĐT đã có trong hệ thống\n");
                    } else if (!customerDto.getPhone().matches("\\d{9,11}")) {
                        error = true;
                        phoneCell.setCellStyle(errStyle);
                        errDetails.append("SĐT không đúng định dạng\n");
                    }

                    if (StringUtils.hasText(birthdayStr)) {
                        try {
                            var birthDay = dateFormat.parse(birthdayStr);
                            if (DateUtils.truncate(new Date(), Calendar.DATE).before(birthDay)) {
                                error = true;
                                birthdayCell.setCellStyle(errStyle);
                                errDetails.append("Ngày sinh không hợp lệ\n");
                            } else {
                                customerDto.setBirthday(birthDay);
                            }
                        } catch (ParseException pse) {
                            error = true;
                            birthdayCell.setCellStyle(errStyle);
                            errDetails.append("Sai định dạng ngày sinh, định dạng đúng: dd/MM/yyyy\n");
                        }

                    }
                    if (!StringUtils.hasText(orgInputId)) {
                        error = true;
                        orgIdCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập Id tổ chức\n");
                    } else {
                        var org = organizationRepository.findById(Integer.valueOf(orgInputId))
                                .orElse(null);
                        if (org == null) {
                            error = true;
                            orgIdCell.setCellStyle(errStyle);
                            errDetails.append("Tổ chức cấp trên không tồn tại\n");
                        } else {
                            if (org.getId() != organizationId && !organizationRepository
                                    .findChildIdRecursiveById(organizationId).contains(org.getId())) {

                                error = true;
                                orgIdCell.setCellStyle(errStyle);
                                errDetails.append("Tổ chức cấp trên phải thuộc tổ chức của bạn hoặc tổ chức con của bạn\n");
                            } else {
                                customerDto.setOrganizationId(org.getId());
                            }
                        }
                    }

                    if (!StringUtils.hasText(roleCode)) {
                        error = true;
                        roleCodeCell.setCellStyle(errStyle);
                        errDetails.append("Chưa nhập mã vai trò\n");
                    } else {
                        var roleList = roleRepository
                                .findByOrganizationId(organizationId);

                        boolean find = false;
                        for (var role : roleList) {
                            if (role.getCode().equals(roleCode)) {
                                find = true;
                                customerDto.setRoleId(role.getId());
                                break;
                            }
                        }

                        if (!find) {
                            error = true;
                            roleCodeCell.setCellStyle(errStyle);
                            errDetails.append("Mã vai trò không tồn tại\n");
                        }

                    }

                    customerList.add(customerDto);
                    errCell.setCellValue(errDetails.toString());

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

                for (var customer : customerList) {
                    create(customer);
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

    public List<SuggestedCustomerDto> suggestCustomer(@RequestParam String name, @CurrentCustomer CustomerUser user) {
        final var org = organizationRepository.findByCustomerId(user.getId()).orElse(null);
        if (org == null) {
            return Collections.emptyList();
        }

        String rootId = (org.getPath() + ".").split("\\.")[0];
        var orgIds = organizationRepository.findAllOrgInTree(Integer.valueOf(rootId));
        return customerRepository.findByOrgListAndNameLike(orgIds, name, SuggestedCustomerDto.class);
    }
}
