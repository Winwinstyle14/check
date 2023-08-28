package com.vhc.ec.contract.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Đối tượng lưu trữ người tham gia vào quy trình ký hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class TemplateRecipientDto implements Serializable {

    private Integer id;

    @Length(max = 63, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @Length(max = 191, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;

    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;

    @Min(1)
    private int role;

    @Length(max = 63, message = "Username '${validatedValue}' must be less than {max} characters long")
    private String username;

    @Length(max = 60, message = "Password '${validatedValue}' must be less than {max} characters long")
    private String password;

    @Min(1)
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
    private Collection<SignTypeDto> signType;

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

    @JsonProperty("card_id")
    private String cardId;

    @JsonProperty("is_otp")
    private Integer isOtp;
    
    @JsonProperty("login_by")
    private String loginBy; 

    @JsonProperty("fields")
    @JsonIgnore
    private Set<TemplateFieldDto> fields; 
}
