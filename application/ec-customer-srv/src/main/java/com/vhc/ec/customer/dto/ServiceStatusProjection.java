package com.vhc.ec.customer.dto;

import java.time.LocalDate;

public interface ServiceStatusProjection {
    Integer getUsageStatus();

    Integer getNumberOfContracts();

    LocalDate getStartDate();

    LocalDate getEndDate();
}
