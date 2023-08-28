package com.vhc.ec.bpmn.definition;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum ContractStatus {
    DRAFF(0),
    CREATED(10),
    PROCESSING(20),
    SIGNED(30),
    REJECTED(31),
    CANCEL(32);

    final Integer dbVal;

    ContractStatus(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }
}
