package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.ParticipantType;

/**
 * Chuyển đổi loại người dùng trong các thành phần tham gia
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class ParticipantTypeConvert extends EnumDbValueConverter<Integer, ParticipantType> {

    public ParticipantTypeConvert() {
        super(ParticipantType.class);
    }
}
