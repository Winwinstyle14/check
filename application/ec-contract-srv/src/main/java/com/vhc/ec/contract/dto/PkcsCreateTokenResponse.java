package com.vhc.ec.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PkcsCreateTokenResponse {
    private String fieldName;

    private String base64TempData;

    private String hexDigestTempFile;

    private String base64Cert;

    private String message;
}
