package com.vhc.ec.contract.thridparty;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.FieldDto;
import com.vhc.ec.contract.service.FieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tp/contracts/fields")
@ApiVersion("1")
@Slf4j
@RequiredArgsConstructor
public class TpFieldController {

    private final FieldService fieldService;

    @GetMapping
    public FieldDto getFieldByRecipient(@RequestParam int recipientId) {
        return fieldService.findByRecipient(recipientId);
    }
}
