package com.vhc.ec.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author: VHC JSC
 * @version: 1.0
 * @since: 1.0
 */
@Entity(name = "users")
@NoArgsConstructor
@Data
@ToString
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @NotBlank(message = "Username is mandatory")
    @Length(max = 63, message = "Username '${validatedValue}' must be less than {max} characters long")
    private String username;

    @Column
    @NotBlank(message = "Email is mandatory")
    @Length(max = 255, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;

    @Column
    @NotBlank(message = "Password is mandatory")
    private String password;

    @Column
    @NotBlank(message = "Phone is mandatory")
    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;

    @Column(name = "group_id")
    @NotBlank(message = "Group is mandatory")
    private Integer groupId;

    @Column
    private int status;

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
