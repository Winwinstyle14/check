package com.vhc.ec.customer.config;

import com.vhc.ec.customer.defination.BaseStatus;
import com.vhc.ec.customer.dto.CustomerDto;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
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

        // Mapping BaseStatus class <------------> Integer
        modelMapper.addConverter(new AbstractConverter<BaseStatus, Integer>() {
            @Override
            protected Integer convert(BaseStatus source) {
                return source.ordinal();
            }
        });

        // Mapping Integer <--------------> BaseStatus
        modelMapper.addConverter(new AbstractConverter<Integer, BaseStatus>() {
            @Override
            protected BaseStatus convert(Integer source) {
                for (BaseStatus status : BaseStatus.values()) {
                    if (status.ordinal() == source) {
                        return status;
                    }
                }
                return null;
            }
        });

        // mapping Map<String, String> <---------> SignImage class
        modelMapper.addConverter(new AbstractConverter<Map<String, String>, CustomerDto.SignImage>() {
            @Override
            protected CustomerDto.SignImage convert(Map<String, String> source) {
                final var signImage = new CustomerDto.SignImage();
                signImage.setBucket(source.get("bucket"));
                signImage.setPath(source.get("path"));

                return null;
            }
        });

        // Mapping SignImage class <---------> Map<String, String>
        modelMapper.addConverter(new AbstractConverter<CustomerDto.SignImage, Map<String, String>>() {

            @Override
            protected Map<String, String> convert(CustomerDto.SignImage source) {
                final var map = new HashMap<String, String>();
                map.put("bucket", source.getBucket());
                map.put("path", source.getPath());

                return map;
            }
        });

        return modelMapper;
    }
}
