package com.vhc.ec.contract.service;
 
import java.util.Date;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhc.ec.contract.dto.ContractOriginalLinkDto;
import com.vhc.ec.contract.entity.ContractOriginalLink;
import com.vhc.ec.contract.repository.ContractOriginalLinkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ContractOriginalLinkService {
	private final  ContractOriginalLinkRepository contractLinkRepository;
	private final ModelMapper modelMapper;
	
	@Transactional
    public ContractOriginalLinkDto create(ContractOriginalLinkDto contractLinkDto) { 
        var contractLink = modelMapper.map(contractLinkDto, ContractOriginalLink.class);
        contractLink.setCreatedAt(new Date());

        final var created = contractLinkRepository.save(contractLink); 

        return modelMapper.map(created, ContractOriginalLinkDto.class);
    }
	
	public Optional<ContractOriginalLinkDto> findByCode(String code) {
        final var contractOptional = contractLinkRepository.findAllByCode(code);
        var contractDtoOptional = contractOptional.map(
                contract -> modelMapper.map(contract, ContractOriginalLinkDto.class)
        );

        if (contractDtoOptional.isPresent()) {
            var contractDto = contractDtoOptional.get(); 

            return Optional.of(contractDto);
        }


        return Optional.empty();
    }
}
