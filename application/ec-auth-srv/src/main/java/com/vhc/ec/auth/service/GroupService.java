package com.vhc.ec.auth.service;

import com.google.common.reflect.TypeToken;
import com.vhc.ec.auth.dto.GroupDto;
import com.vhc.ec.auth.entity.Group;
import com.vhc.ec.auth.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final ModelMapper modelMapper;

    /**
     * create new a group
     *
     * @param groupDto Thông tin chi tiết của nhóm người dùng
     * @return Thông tin của nhóm người dùng sau khi đã tạo
     */
    public GroupDto create(GroupDto groupDto) {
        Group groupToSave = modelMapper.map(groupDto, Group.class);
        Group groupSaved = groupRepository.save(groupToSave);

        return modelMapper.map(groupSaved, GroupDto.class);
    }

    /**
     * get by id
     *
     * @param id Mã số tham chiếu tới nhóm người dùng
     * @return {@link GroupDto} Thông tin chi tiết về nhóm người dùng
     */
    public Optional<GroupDto> getById(int id) {
        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOptional.isPresent()) {
            return Optional.of(
                    modelMapper.map(
                            groupOptional.get(),
                            GroupDto.class)
            );
        }

        return Optional.empty();
    }

    /**
     * Cập nhật thông tin nhóm người dùng
     *
     * @param id       Mã số tham chiếu tới nhóm người dùng
     * @param groupDto Thông tin chi tiết nhóm người dùng cần cập nhật
     * @return Thông tin chi tiết nhóm người dùng sau khi cập nhật
     */
    public Optional<GroupDto> update(int id, GroupDto groupDto) {
        final var groupOptional = groupRepository.findById(id);
        if (groupOptional.isPresent()) {
            final var group = groupOptional.get();
            group.setName(groupDto.getName());
            group.setStatus(groupDto.getStatus());

            final var updated = groupRepository.save(group);
            return Optional.of(
                    modelMapper.map(updated, GroupDto.class)
            );
        }

        return Optional.empty();
    }

    /**
     * Lấy toàn bộ nhóm người dùng
     *
     * @return Danh sách nhóm người dùng
     */
    public Collection<GroupDto> getAll() {
        final var groupCollection = groupRepository.findAll();

        final var type = new TypeToken<GroupDto>() {
        }.getType();

        return modelMapper.map(groupCollection, type);
    }
}
