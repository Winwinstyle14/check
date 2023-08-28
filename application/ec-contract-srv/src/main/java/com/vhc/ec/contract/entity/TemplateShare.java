package com.vhc.ec.contract.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "template_contract_shares")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TemplateShare extends Base implements Serializable {

    @Column
    private String email;

    @Column
    private int status;

    @Column
    private int contractId;

    @Column
    private int organizationId;

    @Column
    private int customerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private transient TemplateContract contract;
}
