package com.vhc.ec.customer.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.customer.dto.*;
import com.vhc.ec.customer.service.CustomerService;
import com.vhc.ec.customer.service.PermissionService;
import com.vhc.ec.customer.service.RoleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController 
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final CustomerService customerService;

    @PostMapping(value = {"/customers/roles", "/internal/customers/roles"})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoleDto> create(
            @CurrentCustomer CustomerUser customerUser,
            @Valid @RequestBody RoleDto roleDto) {
        //final var customerOptional = customerService.getById(customerUser.getId());

       // if (customerOptional.isPresent()) {
            // roleDto.setOrganizationId(customerOptional.get().getOrganizationId());
    	try {
    		final var created = roleService.create(roleDto);

            return ResponseEntity.ok(created);
    	}catch (Exception e) {
			log.error("err", e);
		}
            
       // }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/customers/roles/{id}")
    public ResponseEntity<RoleDto> update(@PathVariable("id") int id, @Valid @RequestBody RoleDto roleDto) {
        final var updated = roleService.update(id, roleDto);
        return updated.map(roleDto1 -> {
            final var permissionCollection = permissionService.findByRoleId(id);
            roleDto1.getPermissions().clear();
            roleDto1.getPermissions().addAll(permissionCollection);

            return ResponseEntity.ok(roleDto1);
        }).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    @GetMapping("/customers/roles/{id}")
    public ResponseEntity<RoleDto> getById(@PathVariable("id") int id) {
        final var roleOptional = roleService.getById(id);

        return roleOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    @GetMapping("/customers/roles/search")
    public ResponseEntity<PageDto<RoleDto>> search(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam(name = "name", defaultValue = "", required = false) String name,
            @RequestParam(name = "code", defaultValue = "", required = false) String code,
            Pageable pageable) {
        final var customerOptional = customerService.getById(customerUser.getId());
        if (customerOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        final var page = roleService.search(
                name, code, customerOptional.get().getOrganizationId(), pageable
        );

        return ResponseEntity.ok(page);
    }

    @GetMapping("/customers/roles/get-by-organization/{orgId}")
    public ResponseEntity<PageDto<RoleDto>> getByOrgId(
            @PathVariable("orgId") int orgId,
            Pageable pageable) {
        var page = roleService.getByOrgId(orgId, pageable);

        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/customers/roles/{id}")
    public ResponseEntity<MessageDto> delete(@PathVariable("id") int id) {
        MessageDto messageDto;
        try {
            messageDto = roleService.delete(id);
        } catch (Exception e) {
            messageDto = MessageDto.builder()
                    .code("01")
                    .message("internal error")
                    .build();
            log.error("can't delete role", e);
        }

        return ResponseEntity.ok(messageDto);
    }

    @PostMapping("/customers/roles/check-name-unique")
    public ResponseEntity<MessageDto> checkNameUnique(
            @Valid @RequestBody RoleNameUniqueRequest request) {
        final var orgOptional = roleService.findByName(request.getName(), request.getOrganizationId());

        var message = MessageDto.builder()
                .code("00")
                .message("role name not exists")
                .build();
        if (orgOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("role name existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/customers/roles/check-code-unique")
    public ResponseEntity<MessageDto> checkNameUnique(
            @Valid @RequestBody RoleCodeUniqueRequest request) {
        final var orgOptional = roleService.findByCode(request.getCode(), request.getOrganizationId());

        var message = MessageDto.builder()
                .code("00")
                .message("role code not exists")
                .build();
        if (orgOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("role code existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }
}
