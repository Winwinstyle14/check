package com.vhc.ec.contract.definition;

import com.vhc.ec.contract.converter.IDbValue;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum ShareType implements IDbValue<Integer> {

    CUSTOMER(1),
    GUEST(2);

    private final Integer dbVal;

    ShareType(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return this.dbVal;
    }
}
