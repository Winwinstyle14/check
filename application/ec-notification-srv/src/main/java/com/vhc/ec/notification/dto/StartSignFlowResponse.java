package com.vhc.ec.notification.dto;

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
@ToString
public class StartSignFlowResponse implements Serializable {

    private final boolean success;

    private final String message;

}
