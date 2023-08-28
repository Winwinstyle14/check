package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UploadFileDto implements Serializable {
    private boolean success;

    private String message;

    @JsonProperty("file_object")
    private Uploaded fileObject;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Uploaded implements Serializable {
        @JsonProperty("file_path")
        private String filePath;

        private String filename;
        private String bucket;
    }
}
