package com.vhc.ec.filemgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class ComposeRequest implements Serializable {

    @JsonProperty("file_collection")
    private Collection<FileRequest> fileRequestCollection;

    @JsonProperty("attach_file_collection")
    private Collection<FileRequest> attachFileRequestCollection;

    @NotBlank
    private String bucket;

    @NotBlank
    @JsonProperty("folder_name")
    private String folderName;

    @Data
    @ToString
    public static class FileRequest implements Serializable {
        private String bucket;
        private String path;
        private String fileName;
    }
}
