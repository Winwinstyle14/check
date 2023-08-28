package com.vhc.ec.notification.smpp.dto;

import java.util.Date;

public class SmsDeliverSMWrapper {

    public String GUID;
    public Date sendDate;
    public String isdn;
    public String content;

	@Override
	public String toString() {
		return "DeliverSMWrapper [GUID=" + GUID + ", sendDate=" + sendDate + ", isdn=" + isdn + ", content=" + content + "]";
	}
}
