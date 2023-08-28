package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.RecipientStatus;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class RecipientStatusConverter extends EnumDbValueConverter<Integer, RecipientStatus> {

    public RecipientStatusConverter() {
        super(RecipientStatus.class);
    }
}
