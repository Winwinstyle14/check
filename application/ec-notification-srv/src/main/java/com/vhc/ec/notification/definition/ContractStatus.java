package com.vhc.ec.notification.definition;

import com.vhc.ec.notification.converter.IDbValue;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum ContractStatus implements IDbValue<Integer> {
    DRAFF(0),
    CREATED(10),
    PROCESSING(20),
    SIGNED(30),
    REJECTED(31),
    CANCEL(32),
    ABOUT_EXPRIRE(1),
    EXPRIRE(2);

    final Integer dbVal;

    ContractStatus(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }
}
