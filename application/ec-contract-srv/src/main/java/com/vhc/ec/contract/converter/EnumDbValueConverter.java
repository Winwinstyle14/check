package com.vhc.ec.contract.converter;

import javax.persistence.AttributeConverter;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public abstract class EnumDbValueConverter<T extends Serializable, E extends Enum<E> & IDbValue<T>>
        implements AttributeConverter<E, T> {

    final Class<E> clazz;

    public EnumDbValueConverter(Class<E> clazz) {
        this.clazz = clazz;
    }

    public T convertToDatabaseColumn(E attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.getDbVal();
    }

    public E convertToEntityAttribute(T dbData) {
        if (dbData == null) {
            return null;
        }

        for (E e : clazz.getEnumConstants()) {
            if (dbData.equals(e.getDbVal())) {
                return e;
            }
        }

        return null;
    }
}
