package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class ParticipantResponse implements Serializable {
    private int id;
    private String name;
    private int type;
    private int status;

    private ContractResponse contract;
}
