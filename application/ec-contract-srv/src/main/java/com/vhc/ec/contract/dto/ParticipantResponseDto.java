package com.vhc.ec.contract.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantResponseDto implements Serializable {
    private int id;
    private String name;
    private int type;
}
