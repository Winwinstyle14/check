package com.vhc.ec.contract.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReferenceId implements Serializable {

    Integer refId;

    Integer contractId;

}
