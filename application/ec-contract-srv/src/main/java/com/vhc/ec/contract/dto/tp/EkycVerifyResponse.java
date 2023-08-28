package com.vhc.ec.contract.dto.tp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EkycVerifyResponse {
    @JsonProperty("verify_result")
    private int verifyResult;

    @JsonProperty("verify_result_text")
    private String verifyResultText;

    private Object sim;

    private Object message;

    @JsonProperty("verification_time")
    private Object verificationTime;

    @JsonProperty("face_loc_cmt")
    private List<Object> faceLocCmt;

    @JsonProperty("face1_angle")
    private Object face1Angle;

    @JsonProperty("face2_angle")
    private Object face2Angle;

    @JsonProperty("face_loc_live")
    private List<Object> faceLocLive;

    @JsonProperty("featureVectorFaceCmt")
    private List<Object> featureVectorFaceCmt;

    @JsonProperty("feature_vector_face_live")
    private List<Object> featureVectorFaceLive;

    @JsonProperty("live_image_status")
    private String liveImageStatus;

    @JsonProperty("face_anti_spoof_status")
    private Object faceAntiSpoofStatus;
}
