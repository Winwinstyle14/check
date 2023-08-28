package com.vhc.ec.admin.integration.qldv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class QldvPackageDto {

    @JsonProperty("package_id")
    private String id;

    @JsonProperty("action")
    private Integer action;

    @JsonProperty("note")
    private String note;

    @JsonProperty("period")
    private Integer period;

    @JsonProperty("quantityPackage")
    private Integer quantity;

    @JsonProperty("packageCode")
    private String code;

    @JsonProperty("startDate")
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate startDate;

    @JsonProperty("endDate")
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate endDate;

    @JsonProperty("fieldID")
    private String fieldId;

    @JsonProperty("messageTypeId")
    private String messageTypeId;

    @JsonProperty("typeNhaMang")
    private String typeNhaMang;

    @JsonProperty("packageName")
    private String name;

    @JsonProperty("packageQuantity")
    private Integer packageQuantity;

    @JsonProperty("BRANDNAME")
    private String brandName;
}
