package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.DocumentType;

/**
 * Chuyển đổi trạng thái của tài liệu.
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class DocumentTypeConverter extends EnumDbValueConverter<Integer, DocumentType> {

    public DocumentTypeConverter() {
        super(DocumentType.class);
    }
}
