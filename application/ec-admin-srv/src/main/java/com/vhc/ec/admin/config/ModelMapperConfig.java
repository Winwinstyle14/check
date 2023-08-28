package com.vhc.ec.admin.config;

import com.vhc.ec.admin.constant.BaseStatus;
import com.vhc.ec.admin.constant.CalculatorMethod;
import com.vhc.ec.admin.constant.OrgStatus;
import com.vhc.ec.admin.dto.SaveOrgReq;
import com.vhc.ec.admin.dto.SaveServicePackageDto;
import com.vhc.ec.admin.dto.ServicePackageOrganizationDto;
import com.vhc.ec.admin.entity.Organization;
import com.vhc.ec.admin.entity.association.ServicePackageOrganization;
import com.vhc.ec.admin.integration.qldv.dto.QldvCustomerDto;
import com.vhc.ec.admin.integration.qldv.dto.QldvPackageDto;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        // create an instance, and config
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);

       modelMapper.addMappings(new PropertyMap<ServicePackageOrganization, ServicePackageOrganizationDto>() {
           @Override
           protected void configure() {
                map().setServiceId(source.getId().getServiceId());
                map().setCode(source.getServicePackage().getCode());
                map().setName(source.getServicePackage().getName());
                map().setDuration(source.getServicePackage().getDuration());
               map().setCalculatorMethod(source.getServicePackage().getCalculatorMethod());
           }
       });

       modelMapper.addMappings(new PropertyMap<QldvCustomerDto, SaveOrgReq>() {
           @Override
           protected void configure() {
               map().setName(source.getName());
               map().setTaxCode(source.getTaxCode());
               map().setShortName(source.getTenantCode());
               map().setEmail(source.getEmail());
               map().setAddress(source.getAddress());
               map().setRepresentative(source.getAdminName());
               map().setPosition(source.getAdminPosition());
               map().setPhone(source.getTel());
               // loi object is not an instance of declaring class
               //map().setCode(StringUtils.isEmpty(source.getTenantCode()) ? "NA" : source.getTenantCode());
               map().setStatus(OrgStatus.ACTIVE);
           }
       });

       modelMapper.addMappings(new PropertyMap<QldvPackageDto, SaveServicePackageDto>() {
           @Override
           protected void configure() {
                map().setCode(source.getCode());
                map().setName(source.getName());
//                int period = source.getPeriod();
//
//                if (period > 0) {
//                    map().setCalculatorMethod(CalculatorMethod.BY_TIME);
//                    map().setDuration(period);
//                } else {
//                    map().setCalculatorMethod(CalculatorMethod.BY_CONTRACT_NUMBERS);
//                    map().setNumberOfContracts(source.getPackageQuantity());
//                }

                map().setStatus(BaseStatus.ACTIVE);

           }
       });

        return modelMapper;
    }


}
