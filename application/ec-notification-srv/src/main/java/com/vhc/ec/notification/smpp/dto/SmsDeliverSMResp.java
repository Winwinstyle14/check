package com.vhc.ec.notification.smpp.dto;

public class SmsDeliverSMResp {

    public String smsId;
    public int smsType;
    public String isdn;
    public String error;
    public boolean isSent;
    public int commandStatus;

	@Override
	public String toString() {
		return "SMSReport_DeliverSMResp [smsId=" + smsId + ", smsType=" + smsType + ", isdn=" + isdn + ", error="
				+ error + ", isSent=" + isSent + ", commandStatus=" + commandStatus + "]";
	}
}
