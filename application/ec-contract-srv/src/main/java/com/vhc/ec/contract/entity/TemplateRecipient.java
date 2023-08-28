package com.vhc.ec.contract.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.validator.constraints.Length;

import com.vhc.ec.contract.converter.RecipientRoleConverter;
import com.vhc.ec.contract.converter.RecipientStatusConverter;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.definition.RecipientStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "template_recipients")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TemplateRecipient extends Base implements Serializable {

    @Column
    @Length(max = 63)
    private String name;

    @Column
    @Length(max = 191)
    private String email;

    @Column
    @Length(max = 15)
    private String phone;

    @Column
    @Length(max = 63)
    private String username;

    @Column
    @Length(max = 60)
    private String password;

    @Column
    @Convert(converter = RecipientRoleConverter.class)
    private RecipientRole role;

    @Column
    private int ordering;

    @Column
    @Convert(converter = RecipientStatusConverter.class)
    private RecipientStatus status;

    @Column(name = "from_at")
    private Date fromAt;

    @Column(name = "due_at")
    private Date dueAt;

    @Column(name = "sign_at")
    private Date signAt;

    @Column(name = "process_at")
    private Date processAt;

    @Column(name = "sign_type", columnDefinition = "jsonb")
    private String signType;

    @Column(name = "notify_type", columnDefinition = "jsonb")
    private String notifyType;

    @Column
    private Integer remind;

    @Column(name = "remind_date")
    private Date remindDate;

    @Column(name = "remind_message")
    private String remindMessage;

    @Column(name = "reason_reject")
    private String reasonReject;
    
    @Column(name = "card_id")
    private String cardId;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    @ToString.Exclude
    private TemplateParticipant participant;

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private Set<TemplateField> fields;

    private Integer isOtp;
    
    @Column(name = "login_by")
    private String loginBy; 

    public void addField(TemplateField field) {
        if (field != null) {
            if (fields == null) {
                fields = new HashSet<>();
            }

            field.setRecipient(this);
            fields.add(field);
        }
    }
}
