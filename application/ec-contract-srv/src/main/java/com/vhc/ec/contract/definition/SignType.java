package com.vhc.ec.contract.definition;

import com.vhc.ec.contract.converter.IDbValue;

public enum SignType implements IDbValue<Integer> {
    IMAGE_AND_OTP(1), USB_TOKEN(2), SIM_PKI(3), HSM(4), EKYC(5);

    final Integer dbVal;

    SignType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}