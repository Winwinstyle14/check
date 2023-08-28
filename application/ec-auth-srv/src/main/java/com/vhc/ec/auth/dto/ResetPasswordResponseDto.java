package com.vhc.ec.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
public class ResetPasswordResponseDto {

    final boolean success;

}
