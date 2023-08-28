package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class FieldUpdateRequest implements Serializable {

    private int id;

    @NotNull
    private String name;

    @NotNull
    private String value;

    @NotNull
    private String font;

    @JsonProperty("font_size")
    private short fontSize;

}
