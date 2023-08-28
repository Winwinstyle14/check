package com.vhc.ec.contract.entity;

import com.vhc.ec.contract.converter.DocumentTypeConverter;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.DocumentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "template_documents")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class TemplateDocument extends Base implements Serializable {

    @Column
    @NotBlank
    @Length(max = 255)
    private String name;

    @Column
    @Convert(converter = DocumentTypeConverter.class)
    private DocumentType type;

    @Column
    @Length(max = 255)
    private String path;

    @Column
    @Length(max = 255)
    private String filename;

    @Column
    @Length(max = 255)
    private String bucket;

    @Column
    private int internal;

    @Column
    private int ordering;

    @Column(name = "contract_id")
    private int contractId;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private BaseStatus status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private TemplateContract contract;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private transient Set<TemplateField> fields;

    @Transient
    private String internalPath;
}
