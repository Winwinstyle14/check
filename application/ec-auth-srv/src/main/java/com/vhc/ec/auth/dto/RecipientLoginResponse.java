package com.vhc.ec.auth.dto;

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
public class RecipientLoginResponse implements Serializable {

    private boolean success;
    private RecipientDto recipient;

}
