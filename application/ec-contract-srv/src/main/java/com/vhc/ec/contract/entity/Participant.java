package com.vhc.ec.contract.entity;

import com.vhc.ec.contract.converter.ParticipantTypeConvert;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ParticipantType;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "participants")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Participant extends Base implements Serializable {

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
    private Set<Recipient> recipients;

    @ManyToOne
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    public void addRecipient(Recipient recipient) {
        if (recipient != null) {
            if (recipients == null) {
                recipients = new HashSet<>();
            }

            recipient.setParticipant(this);
            recipients.add(recipient);
        }
    }
}
