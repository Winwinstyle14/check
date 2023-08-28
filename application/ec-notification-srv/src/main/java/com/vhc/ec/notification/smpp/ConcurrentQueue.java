package com.vhc.ec.notification.smpp;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import com.vhc.ec.notification.entity.Sms;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMWrapper;

public class ConcurrentQueue {

	// Input Queue for SMSs
	public static PriorityBlockingQueue<Sms> inputQueue = new PriorityBlockingQueue<Sms>(1000000, new SMSCompare());

	// Input Queue for received SMS
	public static BlockingQueue<SmsDeliverSMWrapper> receivedSMS = new LinkedBlockingQueue<>();

	static class SMSCompare implements Comparator<Sms> {
		public int compare(Sms one, Sms two) {

			// TODO

            return 1;
        }
    }
}
