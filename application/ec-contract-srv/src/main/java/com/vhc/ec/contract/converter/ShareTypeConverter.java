package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.ShareType;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class ShareTypeConverter extends EnumDbValueConverter<Integer, ShareType> {

    public ShareTypeConverter() {
        super(ShareType.class);
    }

}
