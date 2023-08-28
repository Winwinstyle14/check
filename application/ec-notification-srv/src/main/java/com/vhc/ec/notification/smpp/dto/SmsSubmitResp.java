package com.vhc.ec.notification.smpp.dto;

public class SmsSubmitResp {

    public String smsId;
    public int smsType;
    public String isdn;
    public String error;
    public boolean isSubmitSuccess;
    public int commandStatus;

	@Override
	public String toString() {
		return "SMSReport_SubmitResp [smsId=" + smsId + ", smsType=" + smsType + ", isdn=" + isdn + ", error=" + error
				+ ", isSubmitSuccess=" + isSubmitSuccess + ", commandStatus=" + commandStatus + "]";
	}
}
