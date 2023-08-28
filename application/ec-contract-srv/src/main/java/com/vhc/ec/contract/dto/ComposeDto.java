package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class ComposeDto implements Serializable {

    @JsonProperty("file_collection")
    private final Collection<FileDto> fileRequestCollection;

    @JsonProperty("attach_file_collection")
    private final Collection<FileDto> attachFileRequestCollection;

    private final String bucket;

    @JsonProperty("folder_name")
    private final String folderName;

    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class FileDto implements Serializable {
        private final String bucket;
        private final String path;
        private final String fileName;
    }

}
