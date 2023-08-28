package com.vhc.ec.contract.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.ws.rs.DefaultValue;

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
@Entity(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Contract extends Base implements Serializable {

    @Column
    private String name;

    @Column
    private String code;

    @Column(name = "contract_no")
    private String contractNo;

    @Column(name = "sign_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signTime;

    @Column(name = "alias_url")
    private String aliasUrl;

    @Column(name = "ref_id")
    private Integer refId;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "customer_id")
    private int customerId;

    @Column(name = "organization_id")
    private int organizationId;

    @Column(name = "is_template")
    @DefaultValue("false")
    private boolean isTemplate;

    @Column(name = "status")
    @Convert(converter = ContractStatusConverter.class)
    private ContractStatus status;

    @Column
    private String notes;

    @org.hibernate.annotations.Type(type = "json")
    @Column(name = "ui_config", columnDefinition = "jsonb")
    private Map<String, Object> uiConfig;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Reference> refs;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OrderBy("ordering asc")
    private Set<Participant> participants;

    @Column(name = "reason_reject")
    private String reasonReject;

    private Date cancelDate;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private Set<Document> documents;
    
    @Column(name = "template_contract_id")
    private Integer templateContractId; 
    
    @Column(name = "ceca_push")
    private Integer ceCAPush; 
    
    @Column(name = "ceca_status") 
    private Integer cecaStatus;
    
    @Column(name = "contract_expire_time") 
    @Temporal(TemporalType.TIMESTAMP)
    private Date contractExpireTime;

    @Column(name = "contract_uid")
    private String contractUid;
}
