package com.vhc.ec.admin.service;

import com.vhc.ec.admin.constant.CalculatorMethod;
import com.vhc.ec.admin.dto.*;
import com.vhc.ec.admin.entity.ServicePackage;
import com.vhc.ec.admin.exception.CustomException;
import com.vhc.ec.admin.exception.ErrorCode;
import com.vhc.ec.admin.repository.ServicePackageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicePackageService {
    private final ServicePackageRepository servicePackageRepository;

    private final ModelMapper mapper;

    @Transactional
    public ServicePackageView add(SaveServicePackageDto saveServicePackageDto) {
        valid(0, saveServicePackageDto);
        var servicePackage = mapper.map(saveServicePackageDto, ServicePackage.class);
        servicePackage = servicePackageRepository.save(servicePackage);
        return mapper.map(servicePackage, ServicePackageView.class);
    }

    @Transactional
    public ServicePackageView edit(long id, SaveServicePackageDto saveServicePackageDto) {
        var servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));
        valid(id, saveServicePackageDto);
        servicePackage.setCode(saveServicePackageDto.getCode());
        servicePackage.setName(saveServicePackageDto.getName());
        servicePackage.setTotalBeforeVAT(saveServicePackageDto.getTotalBeforeVAT());
        servicePackage.setTotalAfterVAT(saveServicePackageDto.getTotalAfterVAT());
        servicePackage.setCalculatorMethod(saveServicePackageDto.getCalculatorMethod());
        servicePackage.setType(saveServicePackageDto.getType());
        servicePackage.setDuration(saveServicePackageDto.getDuration());
        servicePackage.setNumberOfContracts(saveServicePackageDto.getNumberOfContracts());
        servicePackage.setDescription(saveServicePackageDto.getDescription());
        servicePackage.setStatus(saveServicePackageDto.getStatus());
        servicePackage = servicePackageRepository.save(servicePackage);
        return mapper.map(servicePackage, ServicePackageView.class);
    }

    public PageDto<ServicePackageView> getList(String code, String name, Long totalBeforeVAT, Long totalAfterVAT, Integer duration,
                                               Integer numberOfContracts, Integer status, Pageable pageable) {

        code = "".equals(code) ? null : code;
        name = "".equals(name)? null : name;

        var page = servicePackageRepository.search(code, name, totalBeforeVAT, totalAfterVAT,
                duration, numberOfContracts, status, pageable);

        return mapper.map(
                page, new TypeToken<PageDto<ServicePackageView>>() {
                }.getType()
        );
    }

    public Optional<ServicePackage> findByCode(String code) {
        return servicePackageRepository.findByCode(code);
    }

    public ServicePackageDetailDto getDetail(long id) {
        var servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        return mapper.map(servicePackage, ServicePackageDetailDto.class);
    }

    public void delete(long id) {
        var servicePackage = servicePackageRepository.findById(id).orElse(null);
        if (servicePackage != null) {
            if (CollectionUtils.isEmpty(servicePackage.getOrganizations())) {
                servicePackageRepository.delete(servicePackage);
            } else {
                throw new CustomException(ErrorCode.SERVICE_IS_USING);
            }
        }
    }

    private void valid(long id, SaveServicePackageDto saveServicePackageDto) {
        if (id > 0) {
            servicePackageRepository.findByCodeAndIdNot(saveServicePackageDto.getCode(), id)
                    .ifPresent((ser) -> {
                        throw new CustomException(ErrorCode.SERVICE_PACKAGE_CODE_IS_EXISTED);
                    });
        } else {
            servicePackageRepository.findByCode(saveServicePackageDto.getCode())
                    .ifPresent((ser) -> {
                        throw new CustomException(ErrorCode.SERVICE_PACKAGE_CODE_IS_EXISTED);
                    });
        }

        if (saveServicePackageDto.getCalculatorMethod() == CalculatorMethod.BY_TIME) {
            if (saveServicePackageDto.getDuration() == null) {
                throw new CustomException(ErrorCode.TIME_USING_OF_SERVICE_REQUIRED);
            }

        } else if (saveServicePackageDto.getCalculatorMethod() == CalculatorMethod.BY_CONTRACT_NUMBERS) {
            if (saveServicePackageDto.getNumberOfContracts() == null) {
                throw new CustomException(ErrorCode.NUMBER_OF_CONTRACTS_REQUIRED);
            }
        }
    }

    public List<ServicePackageView> getAllCodes() {
        return servicePackageRepository.findAll().stream()
                .map(pk -> mapper.map(pk, ServicePackageView.class))
                .collect(Collectors.toList());
    }
}
