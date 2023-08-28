package com.vhc.ec.notification.smpp;

import com.vhc.ec.notification.smpp.dto.SmsDeliverSMWrapper;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMResp;
import com.vhc.ec.notification.smpp.dto.SmsSubmitResp;
import org.smpp.pdu.DeliverSM;

public interface SmsReporter {

	void start();

	void destroy();

	void reportSubmitStatus(SmsSubmitResp report);

	void reportDeliverStatus(SmsDeliverSMResp report);

	void reportReceivedSMS(DeliverSM sm, String sms_content);

	void reportReceivedSMS(SmsDeliverSMWrapper wr);
}
