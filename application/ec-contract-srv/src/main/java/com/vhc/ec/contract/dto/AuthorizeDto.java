package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class AuthorizeDto {

    @JsonProperty("email")
    private String email;

    @JsonProperty("full_name")
    private String fullname;

    private String phone;

    private int role;

    @JsonProperty("recipient_id")
    private int recipientId;

    @JsonProperty("is_replace")
    private boolean replace;

    @JsonProperty("card_id")
    private String cardId;

}
