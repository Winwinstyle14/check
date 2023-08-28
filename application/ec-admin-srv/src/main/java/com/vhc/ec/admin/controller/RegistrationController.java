package com.vhc.ec.admin.controller;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.admin.dto.OrgViewDto;
import com.vhc.ec.admin.dto.RegistrationDto;
import com.vhc.ec.admin.service.OrganizationService;
import com.vhc.ec.api.versioning.ApiVersion;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/admin/registrations/organization")
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
public class RegistrationController {
	private final OrganizationService orgService;
	
	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrgViewDto add(@Valid @RequestBody RegistrationDto registrationReq) {
        return orgService.registration(registrationReq);
    }
}
