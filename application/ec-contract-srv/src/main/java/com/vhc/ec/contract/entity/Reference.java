package com.vhc.ec.contract.entity;

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
@Entity(name = "contract_refs")
@IdClass(ReferenceId.class)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Reference implements Serializable {

    @Id
    @Column(name = "ref_id")
    private Integer refId;

    @Id
    @Column(name = "contract_id")
    private Integer contractId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    @PrePersist
    protected void onCreate() {
        CustomerUser customerUser = (CustomerUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        createdAt = updatedAt = new Date();
        createdBy = updatedBy = customerUser.getId();
    }

    @PreUpdate
    protected void onUpdate() {
        CustomerUser customerUser = (CustomerUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        updatedAt = new Date();
        updatedBy = customerUser.getId();
    }
}
