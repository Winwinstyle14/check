package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiHsmSignReq {
    private List<Integer> recipients;

    private HsmSignRequest hsmSignRequest;

}
