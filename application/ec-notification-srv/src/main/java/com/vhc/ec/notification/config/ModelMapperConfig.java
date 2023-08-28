package com.vhc.ec.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình mapping dữ liệu trên đối tượng thư viện model mapper
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
@Slf4j
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        // create an instance, and config
        ModelMapper modelMapper = new ModelMapper();

        // application config
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }

}
