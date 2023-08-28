package com.vhc.ec.customer.service;

import com.vhc.ec.customer.defination.BaseStatus;
import com.vhc.ec.customer.dto.MessageDto;
import com.vhc.ec.customer.dto.PageDto;
import com.vhc.ec.customer.dto.RoleDto;
import com.vhc.ec.customer.entity.Permission;
import com.vhc.ec.customer.entity.Role;
import com.vhc.ec.customer.repository.CustomerRepository;
import com.vhc.ec.customer.repository.PermissionRepository;
import com.vhc.ec.customer.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;

    public RoleDto create(RoleDto roleDto) {
        final var role = modelMapper.map(
                roleDto,
                Role.class
        );

        final var permissions = Set.copyOf(role.getPermissions());
        if (role.getPermissions() != null) {
            role.getPermissions().clear();
        }

        permissions.forEach(role::addPermission);

        final var created = roleRepository.save(role);

        return modelMapper.map(
                created,
                RoleDto.class
        );
    }

    public Optional<RoleDto> getById(int id) {
        final var roleOptional = roleRepository.findById(id);

        return roleOptional.map(role -> modelMapper.map(role, RoleDto.class));
    }

    @Transactional
    public Optional<RoleDto> update(int id, RoleDto roleDto) {
        final var roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            return Optional.empty();
        }

        // update role
        final var role = roleOptional.get();
        role.setName(roleDto.getName());
        role.setCode(roleDto.getCode());
        role.setDescription(roleDto.getDescription());

        // update status
        final var statusOptional = Arrays.stream(BaseStatus.values()).filter(
                baseStatus -> baseStatus.ordinal() == roleDto.getStatus()
        ).findFirst();

        role.setStatus(
                statusOptional.orElse(BaseStatus.IN_ACTIVE)
        );

        // update permissions
        if (role.getPermissions() != null) {
            role.getPermissions().clear();
        }

        final var updated = roleRepository.save(role);

        final Collection<Permission> permissions = modelMapper.map(
                roleDto.getPermissions(),
                new TypeToken<Collection<Permission>>() {
                }.getType()
        );

        permissions.forEach(permission -> {
            permission.setRole(updated);

            permissionRepository.save(permission);
        });

        return Optional.of(
                modelMapper.map(
                        updated,
                        RoleDto.class
                )
        );
    }

    public PageDto<RoleDto> search(String name, String code, int orgId, Pageable pageable) {
        final var page = roleRepository.search(
                name, code, orgId, pageable
        );

        final var typeToken = new TypeToken<PageDto<RoleDto>>() {
        }.getType();
        return modelMapper.map(page, typeToken);
    }

    @Transactional
    public MessageDto delete(int id) {
        final var customerOptional = customerRepository.findFirstByRoleId(id);

        if (customerOptional.isPresent()) {
            return MessageDto.builder()
                    .code("10")
                    .message("Role was assigned to user. Can't delete this role")
                    .build();
        }

        final var roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            return MessageDto.builder()
                    .code("20")
                    .message("Role not found")
                    .build();
        }

        permissionRepository.deleteAllByRoleId(id);
        roleRepository.delete(roleOptional.get());

        return MessageDto.builder()
                .code("00")
                .message("Role was deleted")
                .build();
    }

    public Optional<RoleDto> findByName(String name, int organizationId) {
        final var roleOptional = roleRepository.findFirstByNameIgnoreCaseAndOrganizationId(name, organizationId);

        return roleOptional.map(role -> modelMapper.map(role, RoleDto.class));
    }

    public Optional<RoleDto> findByCode(String code, int organizationId) {
        final var roleOptional = roleRepository.findFirstByCodeIgnoreCaseAndOrganizationId(code, organizationId);

        return roleOptional.map(role -> modelMapper.map(role, RoleDto.class));
    }

    public PageDto<RoleDto> getByOrgId(int orgId, Pageable pageable) {
        final var page = roleRepository.findByOrganizationIdOrderByNameAsc(orgId, pageable);

        var type = new com.google.common.reflect.TypeToken<PageDto<RoleDto>>() {
        }.getType();
        return modelMapper.map(
                page, type
        );
    }
}
