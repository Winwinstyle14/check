package com.vhc.ec.customer.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "customers")
@Getter
@Setter
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@NoArgsConstructor
@ToString
public class Customer extends Base implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String phone;

    @org.hibernate.annotations.Type(type = "json")
    @Column(name = "sign_image", columnDefinition = "jsonb")
    private List<Map<String, String>> signImage;

    @Column(name = "hsm_name")
    private String hsmName;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "phone_sign")
    private String phoneSign;

    @Column(name = "phone_tel")
    private Integer phoneTel;
    
    @Column(name = "tax_code")
    private String taxCode;
    
    @Column(name = "hsm_pass")
    private String hsmPass;

    @Column
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date birthday;

    @Column(name = "organization_id")
    private int organizationId;
    
    @Column(name = "organization_change")
    private int organizationChange; 

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Organization organization;
}
