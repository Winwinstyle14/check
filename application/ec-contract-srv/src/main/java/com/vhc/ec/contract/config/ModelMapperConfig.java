package com.vhc.ec.contract.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.definition.ParticipantType;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.definition.RecipientStatus;
import com.vhc.ec.contract.dto.SignTypeDto;

import lombok.extern.slf4j.Slf4j;

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
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setUseOSGiClassLoaderBridging(true)
                .setPreferNestedProperties(false)
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE)
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE);

        // convert base status <---> int
        modelMapper.addConverter(new AbstractConverter<BaseStatus, Integer>() {
            @Override
            protected Integer convert(BaseStatus source) {
                return source.ordinal();
            }
        });
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

        // convert contract status <-------> int
        modelMapper.addConverter(new AbstractConverter<ContractStatus, Integer>() {
            @Override
            protected Integer convert(ContractStatus source) {
                return source.getDbVal();
            }
        });
        modelMapper.addConverter(new AbstractConverter<Integer, ContractStatus>() {
            @Override
            protected ContractStatus convert(Integer source) {
                for (ContractStatus status : ContractStatus.values()) {
                    if (status.getDbVal().equals(source)) {
                        return status;
                    }
                }

                return null;
            }
        });

        /*
        modelMapper.addConverter(new AbstractConverter<Set<Participant>, Collection<ParticipantResponseDto>>() {
            @Override
            protected Collection<ParticipantResponseDto> convert(Set<Participant> source) {
                final var dest = new ArrayList<ParticipantResponseDto>();
                for (var participant : source) {
                    var item = modelMapper.map(participant, ParticipantResponseDto.class);
                    dest.add(item);
                }

                return dest;
            }
        });
        */
        /*
         modelMapper.addMappings(new PropertyMap<RecipientDto, FieldDto.RecipientResponse>() {
             @Override
             protected void configure() {
                 skip()
             }
         })

         */

        // convert sign type <------------> string
        modelMapper.addConverter(new AbstractConverter<Collection<SignTypeDto>, String>() {
            @Override
            protected String convert(Collection<SignTypeDto> source) {
                if (source != null && source.size() > 0) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.writeValueAsString(source);
                    } catch (JsonProcessingException e) {
                        log.error(String.format("can't parse sign type object \"%s\" to string", source), e);
                    }
                }

                return null;
            }
        });
        modelMapper.addConverter(new AbstractConverter<String, Collection<SignTypeDto>>() {
            @Override
            protected Collection<SignTypeDto> convert(String source) {
                if (StringUtils.hasText(source)) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return Arrays.asList(objectMapper.readValue(source, SignTypeDto[].class));
                    } catch (JsonProcessingException e) {
                        log.error(String.format("can't parser string \"%s\" to sign type list.", source), e);
                    }
                }

                return Collections.emptyList();
            }
        });

        // convert field type <------------> int
        modelMapper.addConverter(new AbstractConverter<FieldType, Integer>() {
            @Override
            protected Integer convert(FieldType source) {
                return source.getDbVal();
            }
        });
        modelMapper.addConverter(new AbstractConverter<Integer, FieldType>() {
            @Override
            protected FieldType convert(Integer source) {
                for (FieldType type : FieldType.values()) {
                    if (type.getDbVal().equals(source)) {
                        return type;
                    }
                }
                return null;
            }
        });

        // convert participant type <------------> int
        modelMapper.addConverter(new AbstractConverter<ParticipantType, Integer>() {
            @Override
            protected Integer convert(ParticipantType source) {
                return source.getDbVal();
            }
        });
        modelMapper.addConverter(new AbstractConverter<Integer, ParticipantType>() {
            @Override
            protected ParticipantType convert(Integer source) {
                for (ParticipantType type : ParticipantType.values()) {
                    if (type.getDbVal().equals(source)) {
                        return type;
                    }
                }
                return null;
            }
        });

        // convert recipient role <------------> int
        modelMapper.addConverter(new AbstractConverter<RecipientRole, Integer>() {
            @Override
            protected Integer convert(RecipientRole source) {
                return source.getDbVal();
            }
        });
        modelMapper.addConverter(new AbstractConverter<Integer, RecipientRole>() {
            @Override
            protected RecipientRole convert(Integer source) {
                for (RecipientRole role : RecipientRole.values()) {
                    if (role.getDbVal().equals(source)) {
                        return role;
                    }
                }
                return null;
            }
        });

        // convert recipient status <------------> int
        modelMapper.addConverter(new AbstractConverter<RecipientStatus, Integer>() {
            @Override
            protected Integer convert(RecipientStatus source) {
                return source.getDbVal();
            }
        });
        modelMapper.addConverter(new AbstractConverter<Integer, RecipientStatus>() {
            @Override
            protected RecipientStatus convert(Integer source) {
                for (RecipientStatus status : RecipientStatus.values()) {
                    if (status.getDbVal().equals(source)) {
                        return status;
                    }
                }
                return null;
            }
        });

        // convert document type <------------> int
        modelMapper.addConverter(new AbstractConverter<DocumentType, Integer>() {
            @Override
            protected Integer convert(DocumentType source) {
                return source.getDbVal();
            }
        });
        modelMapper.addConverter(new AbstractConverter<Integer, DocumentType>() {
            @Override
            protected DocumentType convert(Integer source) {
                for (DocumentType type : DocumentType.values()) {
                    if (type.getDbVal().equals(source)) {
                        return type;
                    }
                }
                return null;
            }
        });

        return modelMapper;
    }

}
