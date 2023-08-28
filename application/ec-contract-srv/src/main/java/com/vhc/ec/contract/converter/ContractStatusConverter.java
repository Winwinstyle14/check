package com.vhc.ec.contract.converter;

import com.vhc.ec.contract.definition.ContractStatus;

/**
 * Chuyển đổi trạng thái của hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class ContractStatusConverter extends EnumDbValueConverter<Integer, ContractStatus> {

    public ContractStatusConverter() {
        super(ContractStatus.class);
    }
}
