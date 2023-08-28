package com.vhc.ec.contract.service;

import java.util.Collection;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.dto.TypeDto;
import com.vhc.ec.contract.entity.Type;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.TypeRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class TypeService {

    private final TypeRepository typeRepository;
    private final ContractRepository contractRepository;
    private final ModelMapper modelMapper;

    /**
     * create new a contract type
     *
     * @param typeDto {@link TypeDto}
     * @return {@link TypeDto}
     * @see TypeDto
     */
    public Optional<TypeDto> create(TypeDto typeDto) {
        try {
            Type type = modelMapper.map(
                    typeDto,
                    Type.class
            );

            Type created = typeRepository.save(type);

            return Optional.of(
                    modelMapper.map(
                            created,
                            TypeDto.class
                    )
            );
        } catch (Exception e) {
            log.error(String.format("can't create new type %s", typeDto.toString()), e);
        }

        return Optional.empty();
    }

    /**
     * find contract type by id
     *
     * @param id contract type id
     * @return {@link TypeDto}
     * @see TypeDto
     */
    public Optional<TypeDto> findById(int id) {
        Optional<Type> typeOptional = typeRepository.findById(id);

        return typeOptional.map(type -> modelMapper.map(type, TypeDto.class));
    }

    /**
     * find contract type by organization id
     *
     * @param orgId organization id
     * @return {@link TypeDto}
     * @see TypeDto
     */
    public Collection<TypeDto> findByOrgId(int orgId, String name, String code) {
        Collection<Type> typeCollection = typeRepository.findByOrganizationIdOrderByOrdering(
                orgId, name, code
        );

        return modelMapper.map(
                typeCollection,
                new TypeToken<Collection<TypeDto>>() {
                }.getType()
        );
    }

    public Optional<TypeDto> update(int id, TypeDto typeDto) {
        final var typeOptional = typeRepository.findById(id);
        if (typeOptional.isPresent()) {
            final var typeToUpdate = typeOptional.get();
            typeToUpdate.setName(typeDto.getName());
            typeToUpdate.setCode(typeDto.getCode());
            typeToUpdate.setOrganizationId(typeDto.getOrganizationId());
            typeToUpdate.setStatus(
                    typeDto.getStatus() == 1 ?
                            BaseStatus.ACTIVE : BaseStatus.IN_ACTIVE
            );
            typeToUpdate.setOrdering(typeDto.getOrdering());
            typeToUpdate.setCeCAPush(typeDto.getCecaPush());

            final var updated = typeRepository.save(typeToUpdate);
            return Optional.of(
                    modelMapper.map(updated, TypeDto.class)
            );
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<TypeDto> delete(int id) {
        final var typeOptional = typeRepository.findById(id);

        if (typeOptional.isPresent()) {
            final var canDelete = contractRepository.findContractsByTypeId(id).stream().count() == 0;

            // nếu chưa có hợp đồng
            if (canDelete) {
                final var deleted = typeOptional.get();
                typeRepository.delete(deleted);

                deleted.setId(null);
                return Optional.of(
                        modelMapper.map(deleted, TypeDto.class)
                );
            }
        }

        return Optional.empty();
    }

    public Optional<TypeDto> findByOrgIdAndCode(int orgId, String code) {
        final var typeOptional = typeRepository.findFirstByOrganizationIdAndCodeIgnoreCase(
                orgId, code
        );

        return typeOptional.map(type -> modelMapper.map(type, TypeDto.class));
    }

    public Optional<TypeDto> findByOrgIdAndName(int orgId, String name) {
        final var typeOptional = typeRepository.findFirstByOrganizationIdAndNameIgnoreCase(
                orgId, name
        );

        return typeOptional.map(type -> modelMapper.map(type, TypeDto.class));
    }
}
