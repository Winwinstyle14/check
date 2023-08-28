package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.CeCAPushMode;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vhc.ec.admin.constant.OrgStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class RegistrationDto {
	
	private Integer id; 
	
    @Length(max = 600, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name; 
    
    @Length(max = 255, message = "Size '${validatedValue}' must be less than {max} characters long")
    private String size; 
    
    @Length(max = 255, message = "Address '${validatedValue}' must be less than {max} characters long")
    private String address; 
    
    @Length(max = 255, message = "Tax code '${validatedValue}' must be less than {max} characters long")
    @JsonProperty("tax_code")
    private String taxCode; 
    
    @Length(max = 255, message = "Representatives '${validatedValue}' must be less than {max} characters long")
    private String representative; 
    
    @Length(max = 255, message = "Position '${validatedValue}' must be less than {max} characters long")
    private String position;
    
    @Length(max = 191, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;
    
    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;
    
    private OrgStatus status;
    
    private String code;

    private CeCAPushMode ceCAPushMode;
}
