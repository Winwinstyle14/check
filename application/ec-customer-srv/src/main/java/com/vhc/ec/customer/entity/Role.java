package com.vhc.ec.customer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Role extends Base implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @NotBlank
    private String name;

    @Column
    @NotBlank
    private String code;

    @Column
    private String description;

    @Column
    private int organizationId;

    @OneToMany(mappedBy = "role", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Set<Permission> permissions;

    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }

        if (permission != null) {
            permission.setRole(this);
            permissions.add(permission);
        }
    }
}
