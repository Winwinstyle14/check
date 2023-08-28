package com.vhc.ec.contract.entity;

import com.vhc.ec.api.auth.CustomerUser;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.util.Date;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@MappedSuperclass
public class Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
    public void onCreate() {
        try {
            CustomerUser customerUser = (CustomerUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            createdAt = updatedAt = new Date();
            createdBy = updatedBy = customerUser.getId();
        } catch (Exception e) {
        }
    }

    @PreUpdate
    public void onUpdate() {
        try {
            CustomerUser customerUser = (CustomerUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            updatedAt = new Date();
            updatedBy = customerUser.getId();
        } catch (Exception e) {
        }
    }
}
