package com.vhc.ec.contract.entity;

import com.vhc.ec.contract.definition.BaseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Định nghĩa loại hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "types")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Type extends Base implements Serializable {

    @Column
    @NotBlank
    @Length(max = 255)
    private String name;

    @Column
    @NotBlank
    @Length(max = 63)
    private String code;

    @Column(name = "organization_id")
    private int organizationId;

    @Column
    private int ordering;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private BaseStatus status;

    @Column(name = "ceca_push")
    private Integer ceCAPush;
}
