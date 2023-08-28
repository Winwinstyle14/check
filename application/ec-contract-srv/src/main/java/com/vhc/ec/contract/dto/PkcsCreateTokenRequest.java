package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PkcsCreateTokenRequest {

    private int page;

    private float x;

    private float y;

    private float width;

    private float height;

    private String image;

    private String cert;
}
