package com.vhc.ec.filemgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class CreateBucketRequest implements Serializable {

    @JsonProperty("bucket_name")
    @NotBlank(message = "Bucket name is mandatory")
    @Length(max = 63, message = "Bucket name '${validatedValue}' must be less than {max} characters long")
    private String name;

}
