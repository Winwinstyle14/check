package com.vhc.ec.notification.definition;

import com.vhc.ec.notification.converter.IDbValue;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum NoticeStatus implements IDbValue<Integer> {
    NEW(0),
    VIEWED(1);

    final Integer dbVal;

    NoticeStatus(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }
}
