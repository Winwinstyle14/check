package com.vhc.ec.notification.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_log")
@Getter
@Setter
public class SmsLog {

    public SmsLog() {

    }

    public SmsLog(String isdn, String mtcontent, String status, int contractId, int organizationId) {
        this.isdn = isdn;
        this.mtcontent = mtcontent;
        this.status = status;
        this.contractId = contractId;
        this.organizationId = organizationId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_id")
    private long id;

    @Column(name = "isdn")
    private String isdn;

    @Column(name = "mt_content")
    private String mtcontent;

    private String status;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "contract_id")
    private Integer contractId;

    @Column(name = "organization_id")
    private Integer organizationId;
}

