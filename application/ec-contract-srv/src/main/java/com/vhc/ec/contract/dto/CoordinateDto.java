package com.vhc.ec.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CoordinateDto {
	private short page;
 
    private int coordinateX;

    private int coordinateY;

    private int width;

    private int height;
}
