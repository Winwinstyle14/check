package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class CopyFileResponse implements Serializable {

    private String bucket;
    private String filePath;

}
