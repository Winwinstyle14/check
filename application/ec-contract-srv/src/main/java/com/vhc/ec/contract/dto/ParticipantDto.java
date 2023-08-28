package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import java.io.Serializable;
import java.util.Collection;

/**
 * Đối tượng lưu trữ nhóm người dùng tham gia vào quy trình ký hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class ParticipantDto implements Serializable {

    private Integer id;

    @NotNull
    @Length(max = 255, message = "Name '${validatedValue}' must be less than {max}")
    private String name;

    @Min(value = 1, message = "Type '${validatedValue}' must be greater than {value}")
    private int type;

    @Min(value = 1, message = "Ordering '${validatedValue}' must be greater than {value}")
    @DefaultValue("1")
    private int ordering;

    @JsonProperty("status")
    private int status;

    @JsonProperty("contract_id")
    private int contractId;
    
    @JsonProperty("tax_code")
    private String taxCode;

    private Collection<RecipientDto> recipients;
}
