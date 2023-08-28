package com.vhc.ec.notification.entity;

import com.vhc.ec.api.auth.CustomerUser;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "notice")
@Data
@NoArgsConstructor
@ToString
public class Notice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "contract_id")
    private int contractId;

    @Column(name = "message_id")
    private int messageId;

    @Column(name = "message_code")
    private String messageCode;

    @Column(name = "notice_name")
    private String noticeName;

    @Column(name = "notice_content")
    private String noticeContent;

    @Column(name = "notice_url")
    private String noticeUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "notice_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date noticeDate;

    private int status;

    //private String module;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {

        CustomerUser customerUser = (CustomerUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        updatedAt = new Date();
        updatedBy = customerUser.getId();
    }
}
