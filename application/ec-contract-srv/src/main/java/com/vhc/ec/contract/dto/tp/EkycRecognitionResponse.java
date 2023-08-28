package com.vhc.ec.contract.dto.tp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class EkycRecognitionResponse {

    @JsonProperty("result_code")
    private int resultCode;

    private String document;

    private String id;

    private String idconf;

    private String name;

    private String birthday;

    private String sex;

    private String address;

    private String province;

    @JsonProperty("province_code")
    private String provinceCode;

    private String district;

    @JsonProperty("district_code")
    private String districtCode;

    private String precinct;

    @JsonProperty("precinct_code")
    private String precinctCode;

    private String expiry;

    @JsonProperty("id_type")
    private String idType;

    private String ethnicity;

    private String religion;

    @JsonProperty("issue_date")
    private String issueDate;

    @JsonProperty("issue_by")
    private String issueBy;

    private String country;

    private String national;

    @JsonProperty("list_confidences")
    private Object listConfidences;

    private List<String> warning;

    @JsonProperty("warning_msg")
    private List<String> warningMsg;

    @JsonProperty("min_confidence")
    private Object minConfidence;

    private String action;

    @JsonProperty("reason_for_action")
    private Object reasonForAction;
}
