package com.vhc.ec.contract.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CopyFileRequest implements Serializable {

    @NotBlank
    private String bucket;

    @NotBlank
    private String filePath;

}
