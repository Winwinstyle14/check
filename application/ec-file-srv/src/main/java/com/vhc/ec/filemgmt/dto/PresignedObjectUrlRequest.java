package com.vhc.ec.filemgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class PresignedObjectUrlRequest implements Serializable {
    @NotBlank
    @JsonProperty("bucket")
    private String bucket;

    @NotBlank
    @JsonProperty("file_path")
    private String filePath;
}
