package com.vhc.ec.filemgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class PresignedObjectUrlResponse implements Serializable {
    final boolean success;

    @JsonProperty("presigned_url")
    final String presignedUrl;
}
