package com.vhc.ec.contract.dto.tp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EkycVerifyRequest {
    @JsonProperty("image_cmt")
    private String imageCmt;

    @JsonProperty("image_live")
    private String imageLive;
}
