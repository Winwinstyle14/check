package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * validate templagte upload file
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ValidateDto implements Serializable {
    private final Boolean success;
    private final String message;
    private final List<String> detail;

    @JsonIgnore
    private String tempFile;
}
