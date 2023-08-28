package com.vhc.ec.bpmn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Đối tượng lưu trữ thông tin các trường dữ liệu cần điền,
 * trong quá trình ký hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class FieldDto implements Serializable {

    private int id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 255, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    private int type;

    private String value;

    @NotBlank(message = "Font is mandatory")
    @Length(max = 63, message = "Font '${validatedValue}' must be less than {max} characters long")
    private String font;

    @JsonProperty("font_size")
    @Min(value = 1, message = "Font size '${validatedValue}' must be greater than {value}")
    private short fontSize;

    @Min(value = 1, message = "Page number '${validatedValue}' must be greater than {value}")
    private short page;

    @JsonProperty("coordinate_x")
    private float coordinateX;

    @JsonProperty("coordinate_y")
    private float coordinateY;

    private float width;

    private float height;

    private short required;

    private int status;

    @JsonProperty("contract_id")
    private int contractId;

    @JsonProperty("document_id")
    private int documentId;

    @JsonProperty("recipient_id")
    private int recipientId;
}
