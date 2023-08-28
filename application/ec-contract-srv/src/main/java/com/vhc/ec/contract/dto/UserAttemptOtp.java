package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserAttemptOtp {

    private int attempts;

    private LocalDateTime nextAttempt;
}
