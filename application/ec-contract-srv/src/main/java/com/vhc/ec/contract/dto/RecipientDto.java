package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

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
public class RecipientDto implements Serializable, Comparable<RecipientDto> {

    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 63, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @NotBlank(message = "Email is mandatory")
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

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonProperty("process_at")
    private Date processAt;

    @JsonProperty("sign_type")
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
    
    @JsonProperty("template_recipient_id")
    private Integer templateRecipientId;
     
    @JsonProperty("card_id")
    private String cardId;

    @JsonProperty("fields")
    @JsonIgnore
    private Set<FieldDto> fields;

    @JsonProperty("is_otp")
    private Integer isOtp;
    
    @JsonProperty("login_by")
    private String loginBy;

    private Integer authorisedBy;

    //so lan chinh sua ban ghi
    @JsonProperty("change_num")
    private Integer changeNum;

    // dieu phoi -> xem xet -> ky -> van thu
    @Override
    public int compareTo(RecipientDto other) {
        if (role == other.getRole()) {
            return ordering - other.getOrdering();
        }

        return role - other.getRole();
    }
}
