package com.vhc.ec.contract.entity;

import com.vhc.ec.contract.converter.FieldTypeConverter;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.FieldType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "fields")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Field extends Base implements Serializable {

    @Column
    //@NotBlank
    @Length(max = 255)
    private String name;

    @Column
    @Convert(converter = FieldTypeConverter.class)
    private FieldType type;

    @Column
    private String value;

    @Column
    @NotBlank
    @Length(max = 63)
    private String font;

    @Column(name = "font_size")
    @Min(1)
    private short fontSize;

    @Column
    private short page;

    @Column(name = "box_x")
    private float coordinateX;

    @Column(name = "box_y")
    private float coordinateY;

    @Column(name = "box_w")
    private float width;

    @Column(name = "box_h")
    private float height;

    @Column(name = "required")
    private short required;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private BaseStatus status;

    @Column(name = "document_id")
    private int documentId;

    @Column(name = "contract_id")
    private int contractId;

    @Column(name = "recipient_id")
    private Integer recipientId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Recipient recipient;
}
