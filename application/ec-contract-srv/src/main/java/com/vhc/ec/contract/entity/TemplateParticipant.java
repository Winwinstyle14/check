package com.vhc.ec.contract.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.vhc.ec.contract.converter.ParticipantTypeConvert;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ParticipantType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "template_participants")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TemplateParticipant extends Base implements Serializable {

    @Column
    @Convert(converter = ParticipantTypeConvert.class)
    private ParticipantType type;

    @Column
    private String name;

    @Column
    private int ordering;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private BaseStatus status;

    @Column(name = "contract_id")
    private int contractId;
    
    @Column(name = "tax_code")
    private String taxCode;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @OrderBy("role asc, ordering asc, id asc")
    private Set<TemplateRecipient> recipients;

    @ManyToOne
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private TemplateContract contract;

    public void addRecipient(TemplateRecipient recipient) {
        if (recipient != null) {
            if (recipients == null) {
                recipients = new HashSet<>();
            }

            recipient.setParticipant(this);
            recipients.add(recipient);
        }
    }
}
