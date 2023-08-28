package com.vhc.ec.contract.converter;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface IDbValue<T extends Serializable> {

    T getDbVal();

}
