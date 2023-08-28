package com.vhc.ec.admin.service;

import java.time.LocalDate;
import java.util.*;

import com.vhc.ec.admin.constant.*;
import com.vhc.ec.admin.dto.*;
import com.vhc.ec.admin.dto.customer.CustomerDto;
import com.vhc.ec.admin.dto.customer.FindCustomerByEmailRequest;
import com.vhc.ec.admin.dto.customer.PermissionDto;
import com.vhc.ec.admin.dto.customer.RoleDto;
import com.vhc.ec.admin.integration.qldv.dto.QldvCustomerDto;
import com.vhc.ec.admin.integration.qldv.dto.QldvPackageDto;
import com.vhc.ec.admin.integration.qldv.entity.QldvCustomer;
import com.vhc.ec.admin.integration.qldv.entity.QldvPackage;
import com.vhc.ec.admin.integration.qldv.repository.QldvCustomerRepository;
import com.vhc.ec.admin.integration.qldv.repository.QldvPackageRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.vhc.ec.admin.entity.Organization;
import com.vhc.ec.admin.entity.association.ServicePackageOrganization;
import com.vhc.ec.admin.entity.id.ServicePackageOrganizationPK;
import com.vhc.ec.admin.exception.CustomException;
import com.vhc.ec.admin.exception.ErrorCode;
import com.vhc.ec.admin.repository.CustomerRepository;
import com.vhc.ec.admin.repository.OrganizationRepository;
import com.vhc.ec.admin.repository.ServicePackageOrganizationRepository;
import com.vhc.ec.admin.repository.ServicePackageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ServicePackageOrganizationRepository servicePackageOrganizationRepository;
    private final ModelMapper mapper;
    private final ContractService contractService;
    private final RestTemplate restTemplate;
    private final CustomerRepository customerRepository;
    private final UserService userService;
    private final QldvCustomerRepository qldvCustomerRepository;
    private final PasswordEncoder passwordEncoder;

    public PageDto<OrgViewDto> search(String name, String address, String representative, String email,
                                      String phone, Integer status, String code, Pageable pageable) {

        name = "".equals(name) ? null : name;
        address = "".equals(address) ? null : address;
        representative = "".equals(representative) ? null : representative;
        email = "".equals(email) ? null : email;
        phone = "".equals(phone) ? null : phone;

        var page = organizationRepository.search(name, address, representative, email,
                phone, status, pageable);

        return mapper.map(
                page, new TypeToken<PageDto<OrgViewDto>>() {
                }.getType()
        );
    }

    @Transactional
    public OrgViewDto add(SaveOrgReq saveOrgReq) {
        valid(saveOrgReq, 0);
        var org = mapper.map(saveOrgReq, Organization.class);
        org.setCreatedByAdmin(true);
        org.setPath(saveOrgReq.getCode());
        org = organizationRepository.save(org);
        org.setPath(String.valueOf(org.getId()));
        org.setCeCAPushMode(CeCAPushMode.NONE);
        org = organizationRepository.saveAndFlush(org);
        return mapper.map(org, OrgViewDto.class);
    }

    @Transactional
    public OrgViewDto edit(int id, SaveOrgReq saveOrgReq) {
        var org = valid(saveOrgReq, id);
        var customer = customerRepository.findFirstByEmail(saveOrgReq.getEmail());
        int codeInfo = 0;
        if (customer.isEmpty()) {
            codeInfo = 1;
        } else if (customer.get().getOrganizationId() == id) {
            codeInfo = 2;
        } else {
            return new OrgViewDto(3);
        }

        org.setName(saveOrgReq.getName());
        org.setTaxCode(saveOrgReq.getTaxCode());
        org.setShortName(saveOrgReq.getShortName());
        org.setAddress(saveOrgReq.getAddress());
        org.setEmail(saveOrgReq.getEmail());
        org.setRepresentative(saveOrgReq.getRepresentative());
        org.setPosition(saveOrgReq.getPosition());
        org.setSize(saveOrgReq.getSize());
        org.setPhone(saveOrgReq.getPhone());
        org.setStatus(saveOrgReq.getStatus());
        org.setCeCAPushMode(saveOrgReq.getCeCAPushMode());
        org = organizationRepository.save(org);
        var orgView = mapper.map(org, OrgViewDto.class);
        orgView.setCodeInfo(codeInfo);
        return orgView;
    }

    public OrgDetailDto getDetail(int id) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        var orgDetail = mapper.map(org, OrgDetailDto.class);
        return orgDetail;
    }

    @Transactional
    public OrgViewDto active(int id) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        if (org.getStatus() != OrgStatus.ACTIVE) {
            org.setStatus(OrgStatus.ACTIVE);
        }

        org = organizationRepository.save(org);
        var result = mapper.map(org, OrgViewDto.class);
        var customer = customerRepository.findFirstByOrganizationId(id).orElse(null);
        if (customer == null) {
            result.setCodeInfo(4);
        }

        return result;
    }

    @Transactional
    public OrgDetailDto registerService(int id, OrgRegisterServiceDto registerServiceDto) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        long serviceId = registerServiceDto.getServiceId();
        var service = servicePackageRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        var servicePackageOrg = mapper.map(registerServiceDto, ServicePackageOrganization.class);
        servicePackageOrg.setOrganization(org);
        servicePackageOrg.setServicePackage(service);
        servicePackageOrg.setId(new ServicePackageOrganizationPK(serviceId, id));
        var now = LocalDate.now();
        var startDate = registerServiceDto.getStartDate();

        if (startDate == null)  {
            servicePackageOrg.setUsageStatus(UsageStatus.NOT_USED);
        } else if (startDate.isAfter(now)) {
            servicePackageOrg.setUsageStatus(UsageStatus.NOT_USED);
        } else {
            servicePackageOrg.setUsageStatus(UsageStatus.USING);
        }

        org.getServices().add(servicePackageOrg);
        servicePackageOrganizationRepository.save(servicePackageOrg);
        org = organizationRepository.save(org);
        var result = mapper.map(org, OrgDetailDto.class);
        return result;
    }

    @Transactional
    public void cancelService(int id, long serviceId) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        servicePackageRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        var servicePackageOrg = servicePackageOrganizationRepository
                .findById(new ServicePackageOrganizationPK(serviceId, id)).orElse(null);

        if (servicePackageOrg != null) {
            org.getServices().remove(servicePackageOrg);
            servicePackageOrganizationRepository.delete(servicePackageOrg);
        }

        organizationRepository.save(org);
    }

    public OrgServiceDetailDto getServiceDetail(int id, long serviceId) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        var servicePackageOrg = servicePackageOrganizationRepository
                .findById(new ServicePackageOrganizationPK(serviceId, id)).orElse(null);

        if (servicePackageOrg != null && org.getServices().contains(servicePackageOrg)) {
            var orgServiceDetail = mapper.map(servicePackageOrg, OrgServiceDetailDto.class);
            var servicePackage = servicePackageOrg.getServicePackage();
            orgServiceDetail.setCode(servicePackage.getCode());
            orgServiceDetail.setName(servicePackage.getName());
            orgServiceDetail.setTotalBeforeVAT(servicePackage.getTotalBeforeVAT());
            orgServiceDetail.setCalculatorMethod(servicePackage.getCalculatorMethod());
            orgServiceDetail.setDuration(servicePackage.getDuration());
            orgServiceDetail.setNumberOfContracts(servicePackage.getNumberOfContracts());
            orgServiceDetail.setServiceType(servicePackage.getType());
            return orgServiceDetail;
        } else {
            throw new CustomException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteOrganization(int id) {
        long totalContracts = contractService.countMyOrgAndDescendantContract(id);
        if (totalContracts > 0L) {
            throw new CustomException(ErrorCode.ORGANIZATION_HAS_CONTRACT);
        }

        var org = organizationRepository.findById(id).orElse(null);
        if (org != null) {
            organizationRepository.deleteCustomerByOrgId(id);
            servicePackageOrganizationRepository.deleteAll(org.getServices());
            organizationRepository.delete(org);
        }

    }

    private Organization valid(SaveOrgReq saveOrgReq, int id) {
        if (id > 0) {
            var org = organizationRepository.findById(id)
                    .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

            organizationRepository.findFirstByNameAndIdNot(saveOrgReq.getName(), id)
                    .ifPresent(o -> {
                        throw new CustomException(ErrorCode.ORGANIZATION_NAME_IS_EXISTED);
                    });

            organizationRepository.findFirstByEmailAndIdNot(saveOrgReq.getEmail(), id)
                    .ifPresent(o -> {
                        throw new CustomException(ErrorCode.EMAIL_IS_EXISTED);
                    });

//            organizationRepository.findFirstByPhoneAndIdNot(saveOrgReq.getPhone(), id)
//                    .ifPresent(o -> {
//                        throw new CustomException(ErrorCode.PHONE_IS_EXISTED);
//                    });

            organizationRepository.findFirstByTaxCodeAndIdNot(saveOrgReq.getTaxCode(), id)
                    .ifPresent(o -> {
                        throw new CustomException(ErrorCode.TAX_CODE_IS_EXISTED);
                    });
            return org;
        } else {
            organizationRepository.findFirstByName(saveOrgReq.getName())
                    .ifPresent(o -> {
                        throw new CustomException(ErrorCode.ORGANIZATION_NAME_IS_EXISTED);
                    });

            organizationRepository.findFirstByEmail(saveOrgReq.getEmail())
                    .ifPresent(o -> {
                        throw new CustomException(ErrorCode.EMAIL_IS_EXISTED);
                    });

//            organizationRepository.findFirstByPhone(saveOrgReq.getPhone())
//                    .ifPresent(o -> {
//                        throw new CustomException(ErrorCode.PHONE_IS_EXISTED);
//                    });

            organizationRepository.findFirstByTaxCode(saveOrgReq.getTaxCode())
                    .ifPresent(o -> {
                        throw new CustomException(ErrorCode.TAX_CODE_IS_EXISTED);
                    });
        }

        return null;
    }

    @Transactional
    public OrgViewDto registration(RegistrationDto registrationReq) {

        //Generate Code
        if (registrationReq.getCode() == null) {
            int leftLimit = 48; // numeral '0'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 6;
            Random random = new Random();

            String generatedCode = random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            registrationReq.setCode(generatedCode);
        }

        var saveOrgReq = SaveOrgReq.builder()
                .name(registrationReq.getName())
                .email(registrationReq.getEmail())
                .phone(registrationReq.getPhone())
                .taxCode(registrationReq.getTaxCode())
                .build();

        //valid thÃ´ng tin Ä‘Äƒng kÃ½
        organizationRepository.findFirstByName(saveOrgReq.getName())
                .ifPresent(o -> {
                    throw new CustomException(ErrorCode.ORGANIZATION_NAME_IS_EXISTED);
                });

        customerRepository.findFirstByEmail(saveOrgReq.getEmail())
                .ifPresent(o -> {
                    throw new CustomException(ErrorCode.EMAIL_IS_EXISTED);
                });

        customerRepository.findFirstByPhone(saveOrgReq.getPhone())
                .ifPresent(o -> {
                    throw new CustomException(ErrorCode.PHONE_IS_EXISTED);
                });

        organizationRepository.findFirstByTaxCode(saveOrgReq.getTaxCode())
                .ifPresent(o -> {
                    throw new CustomException(ErrorCode.TAX_CODE_IS_EXISTED);
                });

        //Set thÃ´ng tin máº·c Ä‘á»‹nh vÃ  táº¡o tá»• chá»©c á»Ÿ tráº¡ng thÃ¡i no active
        var org = mapper.map(registrationReq, Organization.class);
        org.setCreatedByAdmin(false);
        org.setStatus(OrgStatus.NOT_ACTIVATED);
        //init path
        org.setPath(org.getCode());
        org = organizationRepository.save(org);
        org.setPath(String.valueOf(org.getId()));
        org = organizationRepository.save(org);


        //Láº¥y danh sÃ¡ch ngÆ°á»�i dÃ¹ng quáº£n trá»‹
        List<UserViewDto> userList = userService.findAll();

        //Gá»­i thÃ´ng bÃ¡o sau khi táº¡o xong
        RegistrationNoticeRequest registrationNoticeRequest = RegistrationNoticeRequest.builder()
                .name(registrationReq.getName())
                .size(registrationReq.getSize())
                .address(registrationReq.getAddress())
                .taxCode(registrationReq.getTaxCode())
                .representatives(registrationReq.getRepresentative())
                .position(registrationReq.getPosition())
                .email(registrationReq.getEmail())
                .phone(registrationReq.getPhone())
                .userAdmin(userList)
                .build();

        // sending email notice
        try {
            restTemplate.postForObject("http://ec-notification-srv/api/v1/internal/notification/registrationAccount", registrationNoticeRequest, Object.class);
        } catch (Exception e) {
            log.error("error", e);
        }

        return mapper.map(org, OrgViewDto.class);
    }

    public void updateStartTimeUsing(int id) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        var services = org.getServices();
        for (var srv : services) {
            if (srv.getServicePackage().getCalculatorMethod() == CalculatorMethod.BY_CONTRACT_NUMBERS
                    && srv.getStartDate() == null) {

                srv.setStartDate(LocalDate.now());
                srv.setUsageStatus(UsageStatus.USING);
                servicePackageOrganizationRepository.save(srv);
                break;
            }
        }
    }

    public void syncOrganizationAndServiceFromQldv(QldvCustomerDto qldvCustomerDto) {
        int type = qldvCustomerDto.getType();
        var org = organizationRepository.findFirstByEmail(qldvCustomerDto.getEmail()).orElse(null);
        QldvCustomer qldvCustomer;

        switch (type) {
            case 1:
                var orgViewDto = onNewOrgFromQldv(qldvCustomerDto);
                if (orgViewDto != null) {
                    var roleDto = addRole(orgViewDto);
                    createAdminOrganization(orgViewDto, qldvCustomerDto, roleDto);
                }
                break;
            case 2:
                qldvCustomer = qldvCustomerRepository.findById(qldvCustomerDto.getId()).orElse(null);
                if (qldvCustomer != null) {
                    qldvCustomer.setContractDate(qldvCustomerDto.getContractDate());
                    qldvCustomer.setStartDate(qldvCustomer.getStartDate());
                    String oldEmail = qldvCustomer.getEmail();
                    String newEmail = qldvCustomerDto.getAdminEmail();
                    if (!newEmail.equals(oldEmail) && org != null) {
                        var admin = restTemplate.postForEntity(
                                "http://ec-customer-srv/api/v1/internal/customers/getByEmail",
                                new FindCustomerByEmailRequest(oldEmail),
                                CustomerDto.class
                        ).getBody();

                        admin.setEmail(newEmail);
                        String password = RandomStringUtils.randomAlphabetic(6);
                        String bcryptPassword = passwordEncoder.encode(password);
                        organizationRepository.updateAdminEmailAndPasswordById(admin.getEmail(), bcryptPassword, admin.getId());

                        var accountNoticeRequest = AccountNoticeRequest
                                .builder()
                                .email(admin.getEmail())
                                .name(admin.getName())
                                .accessCode(password)
                                .phone(admin.getPhone())
                                .build();

                        // sending email notice
                        try {
                            restTemplate.postForObject("http://ec-notification-srv/api/v1/internal/notification/customerAccountNotice", accountNoticeRequest, Object.class);
                        } catch (Exception e) {
                            log.error("error", e);
                        }
                    }

                    qldvCustomerRepository.save(qldvCustomer);
                } else {
                    log.warn("cannot find customer with id {}", qldvCustomer.getId());
                }
                break;
            case 3:
                if (org != null) {
                    org.setStatus(OrgStatus.PENDING);
                    org.setEndTime(3);
                    org.setStopServiceDay(LocalDate.now());
                    organizationRepository.save(org);
                }
                break;
            case 4:
                if (org != null) {
                    org.setStatus(OrgStatus.ACTIVE);
                    organizationRepository.save(org);
                }
                break;
            case 5:
                if (org != null) {
                    org.setStatus(OrgStatus.IN_ACTIVE);
                    org.setEndTime(3);
                    org.setStopServiceDay(LocalDate.now());
                    organizationRepository.save(org);
                }
                break;
            case 6:
                if (org != null) {
                    qldvCustomer = qldvCustomerRepository.findById(qldvCustomerDto.getId()).orElse(null);
                    if (qldvCustomer != null) {
                        Set<QldvPackage> qldvPackages = qldvCustomer.getPackages();
                        if (qldvPackages == null) {
                            qldvPackages = new HashSet<>();
                        }

                        for (var pk : qldvCustomerDto.getPackages()) {
                            qldvPackages.add(mapper.map(pk, QldvPackage.class));
                            setNumberItemForService(pk, org);
                        }

                        qldvCustomer.setPackages(qldvPackages);
                        qldvCustomerRepository.save(qldvCustomer);
                    }

                }
                break;

            default:
                break;
        }
    }

    @Transactional
    public OrgViewDto onNewOrgFromQldv(QldvCustomerDto qldvCustomerDto) {
        var qldvCustomer = mapper.map(qldvCustomerDto, QldvCustomer.class);
        qldvCustomerRepository.save(qldvCustomer);
        var org = organizationRepository.findFirstByEmail(qldvCustomerDto.getEmail());
        if (org.isEmpty()) {
            var saveOrgReq = mapper.map(qldvCustomerDto, SaveOrgReq.class);
            String orgCode = StringUtils.isEmpty(qldvCustomerDto.getTaxCode()) ? "NA" : qldvCustomerDto.getTaxCode();
            saveOrgReq.setEmail(qldvCustomerDto.getAdminEmail());
            saveOrgReq.setPhone(qldvCustomer.getAdminTel());
            saveOrgReq.setCode(orgCode);
            saveOrgReq.setStartLicense(qldvCustomerDto.getStartDate());
            saveOrgReq.setEndLicense(qldvCustomerDto.getEndDate());
            return add(saveOrgReq);
        }

        return null;
    }

    private RoleDto addRole(OrgViewDto newOrg) {
        var roleDtoReq = new RoleDto();
        roleDtoReq.setOrganizationId(newOrg.getId());
        roleDtoReq.setName("Admin");
        roleDtoReq.setCode("ADMIN");
        roleDtoReq.setStatus(1);
        initPermissionList(roleDtoReq);
        return restTemplate.postForEntity(
                "http://ec-customer-srv/api/v1/internal/customers/roles",
                roleDtoReq,
                RoleDto.class
        ).getBody();
    }

    private void createAdminOrganization(OrgViewDto newOrg, QldvCustomerDto qldvCustomerDto, RoleDto roleDto) {
        var customerReq = new CustomerDto();
        customerReq.setName(qldvCustomerDto.getAdminName());
        customerReq.setEmail(qldvCustomerDto.getAdminEmail());
        customerReq.setPhone(qldvCustomerDto.getAdminTel());
        customerReq.setOrganizationId(newOrg.getId());
        customerReq.setRoleId(roleDto.getId());
        customerReq.setStatus(1);
        customerReq.setSignImage(new ArrayList<>());
        restTemplate.postForEntity(
                "http://ec-customer-srv/api/v1/internal/customers",
                customerReq,
                CustomerDto.class
        );
    }

    private void initPermissionList(RoleDto roleDtoReq) {
        var permissionList = List.of(
                new PermissionDto("QLVT_04", 1),
                new PermissionDto("QLHD_01", 1),
                new PermissionDto("QLHD_07", 1),
                new PermissionDto("QLTC_03", 1),
                new PermissionDto("QLTC_01", 1),
                new PermissionDto("QLTC_04", 1),
                new PermissionDto("QLVT_01", 1),
                new PermissionDto("QLHD_13", 1),
                new PermissionDto("QLLHD_05", 1),
                new PermissionDto("QLND_04", 1),
                new PermissionDto("QLTC_02", 1),
                new PermissionDto("QLLHD_01", 1),
                new PermissionDto("QLHD_02", 1),
                new PermissionDto("QLHD_12", 1),
                new PermissionDto("QLND_01", 1),
                new PermissionDto("QLHD_06", 1),
                new PermissionDto("QLVT_02", 1),
                new PermissionDto("QLVT_05", 1),
                new PermissionDto("QLLHD_02", 1),
                new PermissionDto("QLHD_10", 1),
                new PermissionDto("QLVT_03", 1),
                new PermissionDto("QLLHD_03", 1),
                new PermissionDto("QLHD_08", 1),
                new PermissionDto("QLND_02", 1),
                new PermissionDto("QLHD_09", 1),
                new PermissionDto("QLND_03", 1),
                new PermissionDto("QLLHD_04", 1),
                new PermissionDto("QLHD_11", 1)
        );

        roleDtoReq.setPermissions(permissionList);
    }

    private void setNumberItemForService(QldvPackageDto pk, Organization org) {
        Integer totalPackagePurchased = org.getTotalPackagePurchased();
        totalPackagePurchased = (totalPackagePurchased == null ? 0 : totalPackagePurchased);
        org.setTotalPackagePurchased(totalPackagePurchased + 1);

        String code = pk.getCode();
        if (code.equals("CHIPHI_DUYTRI")) {
            org.setStartLicense(pk.getStartDate());
            org.setEndLicense(pk.getEndDate());
            organizationRepository.save(org);
            return;
        }
        Integer numberOfContracts = org.getNumberOfContractsCanCreate();
        numberOfContracts = (numberOfContracts == null ? 0 : numberOfContracts);
        Integer totalContractsPurchased = org.getTotalContractsPurchased();
        totalContractsPurchased = (totalContractsPurchased == null ? 0 : totalContractsPurchased);

        int numberOfContractsAdded = 0;

        if (code.equals("eContract_demo")) {
            numberOfContractsAdded = 50;
        } else if (code.startsWith("eContract_")) {
            try {
                numberOfContractsAdded = Integer.parseInt(code.substring(code.indexOf('_') + 1));
            } catch (NumberFormatException ex) {

            }
        }

        if (numberOfContractsAdded > 0) {
            numberOfContracts += numberOfContractsAdded * pk.getQuantity();
            totalContractsPurchased += numberOfContractsAdded * pk.getQuantity();
            org.setNumberOfContractsCanCreate(numberOfContracts);
            org.setTotalContractsPurchased(totalContractsPurchased);
            organizationRepository.save(org);
        }

        Integer numberOfEkyc = org.getNumberOfEkyc();
        numberOfEkyc = (numberOfEkyc == null ? 0 : numberOfEkyc);
        Integer totalEkycPurchased = org.getTotalEkycPurchased();
        totalEkycPurchased = (totalEkycPurchased == null ? 0 : totalEkycPurchased);
        int numberOfEkycAdded = 0;
        Integer numberOfSms = org.getNumberOfSms();
        numberOfSms = (numberOfSms == null ? 0 : numberOfSms);
        Integer totalSmsPurchased = org.getTotalSmsPurchased();
        totalSmsPurchased = (totalSmsPurchased == null ? 0 : totalSmsPurchased);
        int numberOfSmsAdded = 0;

        switch (code) {
            case "eKYC0":
                numberOfEkycAdded = 50;
                break;
            case "eKYC1":
                numberOfEkycAdded = 200;
                break;
            case "eKYC2":
                numberOfEkycAdded = 500;
                break;
            case "eKYC3":
                numberOfEkycAdded = 1000;
                break;
            case "eKYC4":
                numberOfEkycAdded = 2000;
                break;
            case "eKYC5":
                numberOfEkycAdded = 5000;
                break;
            case "SMS1":
                numberOfSmsAdded = 200;
                break;
            case "SMS2":
                numberOfSmsAdded = 500;
                break;
            case "SMS3":
                numberOfSmsAdded = 1000;
                break;
            case "SMS4":
                numberOfSmsAdded = 2000;
                break;
            case "SMS5":
                numberOfSmsAdded = 5000;
                break;

        }

        if (numberOfEkycAdded > 0) {
            org.setNumberOfEkyc(numberOfEkyc + numberOfEkycAdded * pk.getQuantity() );
            org.setTotalEkycPurchased(totalEkycPurchased + numberOfEkycAdded * pk.getQuantity());
            organizationRepository.save(org);
        }

        if (numberOfSmsAdded > 0) {
            org.setNumberOfSms(numberOfSms + numberOfSmsAdded * pk.getQuantity());
            org.setTotalSmsPurchased(totalSmsPurchased + numberOfSmsAdded * pk.getQuantity());
            organizationRepository.save(org);
        }
    }
}
