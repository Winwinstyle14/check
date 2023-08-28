package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Delegate;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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

    private Integer id;

    //@NotBlank(message = "Name is mandatory")
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
    private Integer recipientId;

    //@JsonProperty("recipient")
    //private RecipientDto recipient;

    @JsonProperty("recipient")
    private RecipientResponse recipient;

    @Getter
    @Setter
    @ToString
    public static class RecipientResponse implements Serializable {
        private Integer id;
        private String name;
        private String email;
        private String phone;
        private int role;
        private String username;
        private int ordering;
        private int status;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        @JsonProperty("from_at")
        private Date fromAt;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        @JsonProperty("due_at")
        private Date dueAt;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        @JsonProperty("sign_at")
        private Date signAt;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        @JsonProperty("process_at")
        private Date processAt;

        @JsonProperty("sign_type")
        @Delegate
        private Collection<RecipientResponse.SignType> signType = new ArrayList<>();

        @JsonProperty("notify_type")
        private String notifyType;

        private Integer remind;

        @JsonFormat(pattern = "yyyy/MM/dd")
        @JsonProperty("remind_date")
        private Date remindDate;

        @JsonProperty("remind_message")
        private String remindMessage;

        @JsonProperty("reason_reject")
        private String reasonReject;

        private String cardId;

        @Data
        @NoArgsConstructor
        @ToString
        public static class SignType implements Serializable {
            private int id;
            private String name;
            @JsonProperty("is_otp")
            private boolean isOtp;
        }
    }
}
