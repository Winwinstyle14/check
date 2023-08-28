package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.SignType;

public class SignTypeConvert extends EnumDbValueConverter<Integer, SignType> {

    public SignTypeConvert() {
        super(SignType.class);
    }
}