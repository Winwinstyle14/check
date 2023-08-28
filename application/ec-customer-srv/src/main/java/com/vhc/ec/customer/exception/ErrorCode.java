package com.vhc.ec.customer.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
    EMAIL_IS_EXISTED(1001, "Email is existed"),
    PHONE_IS_EXISTED(1002, "Phone is existed"),
    ORGANIZATION_NAME_IS_EXISTED(1003, "Organization name is existed"),
    ORGANIZATION_NOT_FOUND(1004, "Organization was not found"),
    USER_NOT_FOUND(1005, "User was not found"),
    TAX_CODE_IS_EXISTED(1006, "Tax code is existed"),
    SERVICE_PACKAGE_NOT_FOUND(1007, "Service was not found"),
    SERVICE_PACKAGE_CODE_IS_EXISTED(1008, "Service package code is used"),
    TIME_USING_OF_SERVICE_REQUIRED(1009, "Time using of service can not be null"),
    NUMBER_OF_CONTRACTS_REQUIRED(1010, "Number of contracts can not be null"),
    SERVICE_IS_USING(1011, "Service is using"),
    ORGANIZATION_HAS_CONTRACT(1012, "Organization has contract cannot delete")
    ;

    private final int code;

    private final String message;

    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
