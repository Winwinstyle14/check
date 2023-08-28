package com.vhc.ec.contract.service;

import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.dto.FieldDto;
import com.vhc.ec.contract.entity.Field;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.util.StringUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
@Slf4j
public class FieldService {

    private final ContractRepository contractRepository;
    private final FieldRepository fieldRepository;
    private final ProcessService processService;
    private final ModelMapper modelMapper;

    /**
     * Thêm mới danh sách trường dữ liệu
     *
     * @param fieldDtoCollection Danh sách trường dữ liệu cần thêm mới
     * @return Danh sách trường dữ liệu đã được lưu trữ thành công
     */
    public Collection<FieldDto> create(Collection<FieldDto> fieldDtoCollection) {

        fieldDtoCollection.forEach(fieldDto -> {
            // xu ly o so hop dong
            if (fieldDto.getType() == FieldType.CONTRACT_NO.getDbVal() && fieldDto.getRecipientId() == null) {
                var contractId = fieldDto.getContractId();
                var contract = contractRepository.findById(contractId).orElse(null);
                if (contract != null) {
                    // copy
                    var field = new FieldDto();
                    BeanUtils.copyProperties(fieldDto, field);
                    field.setValue(contract.getContractNo());
                    processService.addNumberContractCell(field);
                    //fieldDto.setValue(contract.getContractNo());
                }
            }
        });

        Iterable<Field> fieldIterable = fieldRepository.saveAll(
                Arrays.asList(
                        modelMapper.map(fieldDtoCollection, Field[].class)
                )
        );

        return Arrays.asList(
                modelMapper.map(
                        fieldIterable,
                        FieldDto[].class
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
    public Optional<FieldDto> update(int id, FieldDto fieldDto) {
        final var fieldOptional = fieldRepository.findById(id);
        if (fieldOptional.isPresent()) {
            final var field = fieldOptional.get(); 
             
            if(fieldDto.getRecipientId() != null && StringUtils.hasText(fieldDto.getValue())) {
            	// cập nhật dữ liệu khi người dùng nhập thông tin
                field.setValue(fieldDto.getValue());
                field.setFont(fieldDto.getFont());
                field.setFontSize(fieldDto.getFontSize());

                final var updated = fieldRepository.save(field);
                // xử lý tệp tin khi người dùng cập nhật dữ liệu
                return processService.process(
                        updated.getId(),
                        ""
                );
            }else {//Trường hợp ô xử lý không gán người xử lý
            	var fieldToUpdate = new Field();
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
                        modelMapper.map(updated, FieldDto.class)
                );
            }
        }
        /**
        if (fieldOptional.isPresent()) {
            final var field = fieldOptional.get();

            var contractOptional = contractRepository.findById(field.getContractId());
            if (contractOptional.isPresent() && contractOptional.get().getStatus() == ContractStatus.DRAFF) {
                var fieldToUpdate = new Field();
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
                        modelMapper.map(updated, FieldDto.class)
                );
            } else {
                // cập nhật dữ liệu khi người dùng nhập thông tin
                field.setValue(fieldDto.getValue());
                field.setFont(fieldDto.getFont());
                field.setFontSize(fieldDto.getFontSize());

                final var updated = fieldRepository.save(field);
                // xử lý tệp tin khi người dùng cập nhật dữ liệu
                return processService.process(
                        updated.getId()
                );
            }
        }
		*/
        return Optional.empty();
    }

    /**
     * Lấy danh sách trường dữ liệu theo hợp đồng
     *
     * @param contractId Mã hợp đồng
     * @return {@link FieldDto} Danh sách trường dữ liệu
     */
    public Collection<FieldDto> findByContract(int contractId) {
        final var fieldCollection = fieldRepository
                .findByContractIdOrderByTypeAsc(contractId);

        return modelMapper.map(
                fieldCollection,
                new TypeToken<Collection<FieldDto>>() {
                }.getType()
        );
    }

    /**
     * Lấy thông tin trường dữ liệu
     *
     * @param id Mã tham chiếu tới trường dữ liệu
     * @return Thông tin chi tiết của trường dữ liệu
     */
    public Optional<FieldDto> getById(int id) {
        final var fieldOptional = fieldRepository.findById(id);

        return fieldOptional.map(field -> modelMapper.map(field, FieldDto.class));
    }

    /**
     * delete field by id
     *
     * @param id id of field need to delete
     * @return {@link FieldDto}
     */
    public Optional<FieldDto> deleteById(int id) {
        final var fieldOptional = fieldRepository.findById(id);
        if (fieldOptional.isPresent()) {
            var fieldDto = modelMapper.map(fieldOptional.get(), FieldDto.class);

            fieldRepository.delete(fieldOptional.get());
            fieldDto.setId(null);

            return Optional.of(fieldDto);
        }

        return Optional.empty();
    }

    public FieldDto findByRecipient(int recipientId) {
        var field = fieldRepository.findByRecipientId(recipientId).orElse(new Field());
        return modelMapper.map(field, FieldDto.class);
    }
}
