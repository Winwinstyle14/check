package com.vhc.ec.contract.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpMessageDto {
    private boolean success;

    private String message;

    private boolean locked;

    private LocalDateTime nextAttempt;
}
