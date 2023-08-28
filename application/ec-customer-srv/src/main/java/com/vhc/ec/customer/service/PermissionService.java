package com.vhc.ec.customer.service;

import com.google.common.reflect.TypeToken;
import com.vhc.ec.customer.dto.PermissionDto;
import com.vhc.ec.customer.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;

    public Collection<PermissionDto> findByRoleId(int roleId) {
        final var permissionCollection = permissionRepository.findByRoleId(roleId);

        var typeToken = new TypeToken<Collection<PermissionDto>>() {
        }.getType();

        return modelMapper.map(permissionCollection, typeToken);
    }

}
