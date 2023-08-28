package com.vhc.ec.notification.smpp.dto;

import com.vhc.ec.notification.entity.Sms;
import org.smpp.pdu.SubmitSM;

public class SmsSubmitWrapper {

	public Sms sms;
	public SubmitSM submitSM;
}
