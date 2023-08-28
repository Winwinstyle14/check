package com.vhc.ec.contract.definition;

import com.vhc.ec.contract.converter.IDbValue;

/**
 * Phân loại thành phần tham gia vào quá trình ký hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum ParticipantType implements IDbValue<Integer> {
    MY_ORGANIZATION(1), ORGANIZATION(2), PERSONAL(3);

    final Integer dbVal;

    ParticipantType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}
