package com.vhc.ec.contract.controller;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.ContractOriginalLinkDto;
import com.vhc.ec.contract.service.ContractOriginalLinkService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/handle")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
public class ContractOriginalLinkController {
	
	private final ContractOriginalLinkService contractLinkService;
	
	@PostMapping(value = {"/internal"})
    @ResponseStatus(HttpStatus.CREATED)
    public ContractOriginalLinkDto create(
            @Valid @RequestBody ContractOriginalLinkDto contractDto) {
        return contractLinkService.create(contractDto);
    }
	
	@GetMapping(value = {"/{code}"})
    public ResponseEntity<ContractOriginalLinkDto> findByCode(@PathVariable String code) {
		final var contractDtoOptional = contractLinkService.findByCode(code);
		
		if(contractDtoOptional.isPresent()) {
			var contractLinkDto = contractDtoOptional.get();
			
			return ResponseEntity.ok(contractLinkDto);
		}
		
		return ResponseEntity.noContent().build();
	}
}
