package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.FieldType;

/**
 * Chuyển đổi loại hình trường dữ liệu
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class FieldTypeConverter extends EnumDbValueConverter<Integer, FieldType> {

    public FieldTypeConverter() {
        super(FieldType.class);
    }
}
