package com.vhc.ec.contract.definition;

import com.vhc.ec.contract.converter.IDbValue;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum DocumentType implements IDbValue<Integer> {

    PRIMARY(1),
    FINALLY(2),
    ATTACH(3),
    BATCH(4),
    COMPRESS(5),
    BACKUP(6),
    IMG_EKYC(7);

    final Integer dbVal;

    DocumentType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}
