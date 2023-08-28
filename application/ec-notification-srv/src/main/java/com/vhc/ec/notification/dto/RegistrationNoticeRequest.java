package com.vhc.ec.notification.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RegistrationNoticeRequest {
	
	private String name;  
	
    private String size; 
     
    private String address; 
    
    private String taxCode; 
    
    private String representatives; 
    
    private String position;
    
    private String email;
    
    private String phone;
    
    private List<UserViewDto> userAdmin;
}
