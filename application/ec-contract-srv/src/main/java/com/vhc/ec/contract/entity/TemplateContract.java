package com.vhc.ec.contract.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import com.vhc.ec.contract.converter.ContractStatusConverter;
import com.vhc.ec.contract.definition.ContractStatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Đối tượng ánh xạ tới bảng "contracts"
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "template_contracts")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TemplateContract extends Base implements Serializable {

    @Column
    @NotBlank
    @Length(max = 191)
    private String name;

    @Column
    @Length(max = 100)
    private String code;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "customer_id")
    private int customerId;

    @Column(name = "organization_id")
    private int organizationId;

    @Column(name = "status")
    @Convert(converter = ContractStatusConverter.class)
    private ContractStatus status;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OrderBy("ordering asc")
    private Set<TemplateParticipant> participants;
    
    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private Set<TemplateDocument> documents;

    @Column(name = "ceca_push")
    private Integer ceCAPush;
}
