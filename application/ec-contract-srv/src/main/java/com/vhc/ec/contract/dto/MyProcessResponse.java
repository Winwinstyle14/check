package com.vhc.ec.contract.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.vhc.ec.contract.entity.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class MyProcessResponse implements Serializable {

    private int id;
    private String name;
    private String email;
    private String phone;
    private int role;
    private int ordering;
    private int status;
    
    @JsonProperty("ceca_push")
    private Integer cecaPush;
    
    @JsonProperty("ceca_status")
    private Integer cecaStatus;
    
    @JsonProperty("sign_type")
    @Delegate
    private Collection<SignTypeDto> signType;

    private ParticipantResponse participant;

    private Set<MyProcessResField> fields;

    private String cardId;
}
