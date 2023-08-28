package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.RecipientRole;

/**
 * Chuyển đổi vai trò của người tham gia xử lý hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class RecipientRoleConverter extends EnumDbValueConverter<Integer, RecipientRole> {

    public RecipientRoleConverter() {
        super(RecipientRole.class);
    }
}
