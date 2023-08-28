package com.vhc.ec.customer.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ToString
public class OrganizationPhoneUniqueRequest implements Serializable {
    @NotBlank
    private String phone;
}
