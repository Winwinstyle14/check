package com.vhc.ec.bpmn.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Thông báo trả về cho người dùng cuối.
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {

    private boolean success;
    private String message;

}
