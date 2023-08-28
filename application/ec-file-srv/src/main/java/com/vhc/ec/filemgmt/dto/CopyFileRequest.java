package com.vhc.ec.filemgmt.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class CopyFileRequest implements Serializable {

    @NotBlank
    private String bucket;

    @NotBlank
    private String filePath;

}
