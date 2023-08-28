package com.vhc.ec.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class CustomerPhoneTelUniqueRequest implements Serializable {
    @JsonProperty("phone_tel")
    @NotBlank
    private String phoneTel;
}
