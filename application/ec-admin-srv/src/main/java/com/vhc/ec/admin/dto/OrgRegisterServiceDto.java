package com.vhc.ec.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vhc.ec.admin.constant.PaymentStatus;
import com.vhc.ec.admin.constant.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrgRegisterServiceDto {

    private long serviceId;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate purchaseDate;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate paymentDate;

    private PaymentType paymentType;

    private PaymentStatus paymentStatus;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;
}
