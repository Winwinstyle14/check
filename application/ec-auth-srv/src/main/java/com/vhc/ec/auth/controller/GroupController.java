package com.vhc.ec.auth.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.auth.dto.GroupDto;
import com.vhc.ec.auth.service.GroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("groups")
@Consumes(value = "application/json")
@Produces(value = "application/json")
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    /**
     * Thêm mới nhóm người dùng
     *
     * @param groupDto Thông tin nhóm người dùng cần tạo
     * @return {@link GroupDto} T
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GroupDto> create(@Valid @RequestBody final GroupDto groupDto) {
        final var group = groupService.create(groupDto);
        return ResponseEntity.ok(group);
    }

    /**
     * Cập nhật thông tin nhóm người dùng
     *
     * @param id       Mã số tham chiếu tới nhóm người dùng
     * @param groupDto Thông tin chi tiết nhóm người dùng cần cập nhật
     * @return {@link GroupDto}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GroupDto> update(@PathVariable("id") int id, @Valid @RequestBody final GroupDto groupDto) {
        final var groupOptional = groupService.update(id, groupDto);
        return groupOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Lấy thông tin chi tiết nhóm người dùng
     *
     * @param id Mã số tham chiếu tới nhóm người dùng
     * @return Thông tin chi tiết của nhóm người dùng
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable @Min(1) int id) {
        final var groupDtoOptional = groupService.getById(id);

        return groupDtoOptional.map(ResponseEntity::ok).orElseGet(() ->
                ResponseEntity.badRequest().build());
    }

}
