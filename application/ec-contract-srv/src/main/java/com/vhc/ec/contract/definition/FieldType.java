package com.vhc.ec.contract.definition;

import com.vhc.ec.contract.converter.IDbValue;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum FieldType implements IDbValue<Integer> {
    TEXT(1), IMAGE_SIGN(2), DIGITAL_SIGN(3), CONTRACT_NO(4);

    final Integer dbVal;

    FieldType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}
