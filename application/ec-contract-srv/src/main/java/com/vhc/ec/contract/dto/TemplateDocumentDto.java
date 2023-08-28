package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Đối tượng lưu trữ tệp tin của hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TemplateDocumentDto implements Serializable {

    private int id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 255, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @Min(value = 1, message = "Type '${validatedValue}' must be greater than {value}")
    private int type;

    @NotBlank(message = "Path is mandatory")
    @Length(max = 255, message = "Path '${validatedValue}' must be less than {max} characters long")
    private String path;

    @NotBlank(message = "Filename is mandatory")
    @Length(max = 255, message = "Filename '${validatedValue}' must be less than {max} characters long")
    private String filename;

    @NotBlank(message = "Bucket is mandatory")
    @Length(max = 255, message = "Bucket '${validatedValue}' must be less than {max} characters long")
    private String bucket;

    @JsonProperty("internal_path")
    private String internalPath;

    private int internal;

    private int ordering;

    private int status;

    @JsonProperty("contract_id")
    private int contractId;
}
