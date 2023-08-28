package com.vhc.ec.filemgmt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Thông tin tệp tin cần lưu trữ
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class Base64UploadRequest {
    @NotNull
    private String name;

    @NotNull
    private String content;
}
