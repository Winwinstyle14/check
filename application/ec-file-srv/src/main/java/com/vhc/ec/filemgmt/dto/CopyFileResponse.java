package com.vhc.ec.filemgmt.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CopyFileResponse implements Serializable {

    private String bucket;
    private String filePath;

}
