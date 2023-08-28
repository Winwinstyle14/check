package com.vhc.ec.customer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LicenseInfoDto {
    private LocalDate startLicense;

    private LocalDate endLicense;
}
