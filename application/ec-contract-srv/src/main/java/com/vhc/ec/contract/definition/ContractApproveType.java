package com.vhc.ec.contract.definition;

import com.vhc.ec.contract.converter.IDbValue;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum ContractApproveType implements IDbValue<Integer> {

    DEFAULT(0),
    APPROVAL(1),
    REJECT(2);

    private final Integer dbVal;

    ContractApproveType(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }
}