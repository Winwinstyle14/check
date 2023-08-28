package com.vhc.ec.contract.service;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.dto.FieldDto;
import com.vhc.ec.contract.dto.TemplateFieldDto;
import com.vhc.ec.contract.entity.TemplateField;
import com.vhc.ec.contract.repository.TemplateContractRepository;
import com.vhc.ec.contract.repository.TemplateFieldRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class TemplateFieldService {
    private final TemplateFieldRepository fieldRepository;
    private final TemplateContractRepository contractRepository;
    private final ModelMapper modelMapper;

    /**
     * Thêm mới danh sách trường dữ liệu
     *
     * @param fieldDtoCollection Danh sách trường dữ liệu cần thêm mới
     * @return Danh sách trường dữ liệu đã được lưu trữ thành công
     */

    @Transactional
    public Collection<TemplateFieldDto> create(Collection<TemplateFieldDto> fieldDtoCollection) {

        for (var field : fieldDtoCollection) {
            // xoa o ky cu
            if (field.getRecipientId() != null) {
                fieldRepository.deleteByRecipientId(field.getRecipientId());
            }
        }
        Iterable<TemplateField> fieldIterable = fieldRepository.saveAll(
                Arrays.asList(
                        modelMapper.map(fieldDtoCollection, TemplateField[].class)
                )
        );

        return Arrays.asList(
                modelMapper.map(
                        fieldIterable,
                        TemplateFieldDto[].class
                )
        );
    }

    /**
     * Cập nhật thông tin trường dữ liệu
     *
     * @param id       Mã trường dữ liệu
     * @param fieldDto Thông tin cần cập nhật
     * @return Thông tin chi tiết trường dữ liệu
     */
    public Optional<TemplateFieldDto> update(int id, TemplateFieldDto fieldDto) {
        final var fieldOptional = fieldRepository.findById(id);

        if (fieldOptional.isPresent()) {
            final var field = fieldOptional.get();

            var contractOptional = contractRepository.findById(field.getContractId());
            if (contractOptional.isPresent()) {
                var fieldToUpdate = new TemplateField();
                BeanUtils.copyProperties(fieldDto, fieldToUpdate, "id", "type", "status", "recipient");
                var type = modelMapper.map(fieldDto.getType(), FieldType.class);
                var status = modelMapper.map(fieldDto.getStatus(), BaseStatus.class);

                // update properties
                fieldToUpdate.setId(id);
                fieldToUpdate.setType(type);
                fieldToUpdate.setStatus(status);
                fieldToUpdate.setCreatedBy(field.getCreatedBy());
                fieldToUpdate.setCreatedAt(field.getCreatedAt());

                var updated = fieldRepository.save(fieldToUpdate);
                return Optional.of(
                        modelMapper.map(updated, TemplateFieldDto.class)
                );
            }
        }

        return Optional.empty();
    }

    /**
     * Lấy danh sách trường dữ liệu theo hợp đồng
     *
     * @param contractId Mã hợp đồng
     * @return {@link TemplateFieldDto} Danh sách trường dữ liệu
     */
    public Collection<TemplateFieldDto> findByContract(int contractId) {
        final var fieldCollection = fieldRepository
                .findByContractIdOrderByCoordinateYAsc(contractId);

        return modelMapper.map(
                fieldCollection,
                new TypeToken<Collection<TemplateFieldDto>>() {
                }.getType()
        );
    }

    /**
     * delete field by id
     *
     * @param id id of field need to delete
     * @return {@link FieldDto}
     */
    public Optional<TemplateFieldDto> deleteById(int id) {
        final var fieldOptional = fieldRepository.findById(id);
        if (fieldOptional.isPresent()) {
            var fieldDto = modelMapper.map(fieldOptional.get(), TemplateFieldDto.class);

            fieldRepository.delete(fieldOptional.get());
            fieldDto.setId(null);

            return Optional.of(fieldDto);
        }

        return Optional.empty();
    }

}