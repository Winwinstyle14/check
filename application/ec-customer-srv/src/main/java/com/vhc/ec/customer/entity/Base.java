package com.vhc.ec.customer.entity;

import com.vhc.ec.customer.defination.BaseStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Đối tượng dùng chung, các class mapping dữ liệu extend từ lớp này
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@MappedSuperclass
public class Base {

    @Column(columnDefinition = "int4")
    @Enumerated(EnumType.ORDINAL)
    private BaseStatus status;

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
        createdAt = updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
