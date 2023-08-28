package com.vhc.ec.filemgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Thông tin tệp tin người sử dụng tải lên hệ thống
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
public class UploadFileResponse implements Serializable {

    private final boolean success;

    private final String message;

    @JsonProperty("file_object")
    private Uploaded fileObject;

    @Data
    @Builder
    @ToString
    public static class Uploaded {
        @JsonProperty("file_path")
        private final String filePath;

        private final String filename;

        private final String bucket;
    }
}
