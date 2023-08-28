package com.vhc.ec.admin.integration.qldv.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class QldvPackage {
    @Id
    private String id;

    private Integer action;

    private String note;

    private Integer period;

    private Integer quantity;

    private String code;

    private LocalDate startDate;

    private LocalDate endDate;

    private String fieldId;

    private String messageTypeId;

    private String typeNhaMang;

    private String name;

    private Integer packageQuantity;

    private String brandName;

    private long ecServiceId;
}
