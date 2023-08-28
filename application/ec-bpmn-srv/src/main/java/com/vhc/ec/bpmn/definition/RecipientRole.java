package com.vhc.ec.bpmn.definition;

import com.vhc.ec.bpmn.converter.IDbValue;

/**
 * Vai trò của người tham gia ký hơp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum RecipientRole implements IDbValue<Integer> {
    COORDINATOR(1), REVIEWER(2), SIGNER(3), ARCHIVER(4);

    final Integer dbVal;

    RecipientRole(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}
