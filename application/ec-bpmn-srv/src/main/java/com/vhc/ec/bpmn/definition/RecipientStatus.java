package com.vhc.ec.bpmn.definition;

import com.vhc.ec.bpmn.converter.IDbValue;

/**
 * Trạng thái của người xử lý hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum RecipientStatus implements IDbValue<Integer> {

	DEFAULT(0), PROCESSING(1), APPROVAL(2), REJECT(3), AUTHORIZE(4);

    private final Integer dbValue;

    RecipientStatus(Integer dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public Integer getDbVal() {
        return dbValue;
    }
}
