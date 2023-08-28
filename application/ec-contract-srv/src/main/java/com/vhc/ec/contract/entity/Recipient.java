package com.vhc.ec.contract.entity;

import com.vhc.ec.contract.converter.RecipientRoleConverter;
import com.vhc.ec.contract.converter.RecipientStatusConverter;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.definition.RecipientStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "recipients")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Recipient extends Base implements Serializable {

    @Column
    @NotBlank
    @Length(max = 63)
    private String name;

    @Column
    @NotBlank
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
    
    @Column(name = "template_recipient_id")
    private Integer templateRecipientId;
    
    @Column(name = "card_id")
    private String cardId;

    private Integer isOtp;

    private Integer attempts;

    private LocalDateTime nextAttempt;
    
    @Column(name = "login_by")
    private String loginBy;

    // duoc ai uy quyen
    private Integer authorisedBy;

    // uy quyen cho ai
    private Integer delegateTo;

    //so lan chinh sua ban ghi
    @Column(name = "change_num")
    private Integer changeNum;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    @ToString.Exclude
    private Participant participant;

    //@OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private Set<Field> fields;

    public void addField(Field field) {
        if (field != null) {
            if (fields == null) {
                fields = new HashSet<>();
            }

            field.setRecipient(this);
            fields.add(field);
        }
    }

    public Integer getAttempts() {
        return attempts != null ? attempts : 0;
    }
}
