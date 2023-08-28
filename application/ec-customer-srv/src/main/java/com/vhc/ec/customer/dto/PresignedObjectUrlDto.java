package com.vhc.ec.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Thông tin chi tiết đường dẫn tạm được sinh ra trên hệ thống.
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class PresignedObjectUrlDto {
    private boolean success;

    @JsonProperty("presigned_url")
    private String presignedUrl;
}
