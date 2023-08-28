package com.vhc.ec.contract.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.CeCAResponse;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.service.CeCAService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kết nối Cổng chứng thực hợp đồng điện tử(CeCA)
 * @author VHC_TUANANH
 *
 */
@RestController
@RequestMapping("/ceca")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class CeCAController {
	private final CeCAService ceCAService;
	
	@PostMapping("/receive")
	public ResponseEntity<MessageDto> receiveCeCA(
            @RequestBody CeCAResponse ceCARequest) { 
		final var response = ceCAService.receiveCeCA(ceCARequest);
		
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/request-to-ceca/{id}")
	public ResponseEntity<MessageDto> requestCeCA(
            @PathVariable("id") int contractId) {
		log.info("==> eContract gửi file hợp đồng sang BCT ContractId = "+ contractId);
		
		final var response = ceCAService.requestCeCA(contractId);
		
		if(response.isPresent()) {
			final var ceCAResponse = response.get();
			
			if(ceCAResponse.getStatus().equals("1")) {
				return ResponseEntity.ok(
						MessageDto.builder()
						.success(true)
						.message(response.get().getMessage())
						.build()
					);
			}else {
				return ResponseEntity.ok(
						MessageDto.builder()
						.success(false)
						.message(response.get().getMessage())
						.build()
					);
			}
		}
		
		return ResponseEntity.ok(
				MessageDto.builder()
				.success(false)
				.message("Contract does not exist.")
				.build()
			);
    }
}
