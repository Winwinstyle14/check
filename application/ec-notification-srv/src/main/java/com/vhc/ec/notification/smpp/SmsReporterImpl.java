package com.vhc.ec.notification.smpp;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.vhc.ec.notification.config.SmppConfig;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMWrapper;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMResp;
import com.vhc.ec.notification.smpp.dto.SmsSubmitResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpp.pdu.DeliverSM;


public class SmsReporterImpl implements SmsReporter {

	private static Logger logger = LoggerFactory.getLogger(SmsReporterImpl.class);

	private SmppConfig config;

    private BlockingQueue<SmsSubmitResp> submitSMRespQueue = new LinkedBlockingQueue<>();

    private BlockingQueue<SmsDeliverSMResp> deliverSMRespQueue = new LinkedBlockingQueue<>();

    private Set<String> existedReceiveSMS = new HashSet<String>();

    private List<Thread> threadList = new LinkedList<>();

    public SmsReporterImpl(SmppConfig cf) {
        this.config = cf;
    }

    private static boolean RUN = true;

    /**
     * start thread submit, deliver, receiver
     */
    @Override
    public void start() {

    	// submit message thread
		for (int i = 1; i <= 4; i++) {

			threadList.add(new Thread(new Runnable() {

				@Override
				public void run() {
					while (RUN) {
						try {
							SmsSubmitResp rep = submitSMRespQueue.take();
							if (rep == null)
								continue;

							boolean isOK;
							try {
								isOK = SMS_StatusReport.submitSMStatusReport(config, rep);
								logger.info("Report submit sms status: " + rep.isdn);
							} catch (Exception e) {
								isOK = false;
								logger.error("Report submit sms failed: {}, {}", rep.isdn, isOK, e);
							}

							/*if (!isOK && config.SmsTracker_NON_STOP_RETRY_IF_FAIL == 1)
								submitSMRespQueue.put(rep);*/
						} catch (Exception e) {
							logger.error("SMS Reporter: ", e);
						}
					}
				}
			}));
		}

		// deliver message thread
		for (int i = 1; i <= 3; i++) {

			threadList.add(new Thread(new Runnable() {

				@Override
				public void run() {
					while (RUN) {
						try {
							SmsDeliverSMResp rep = deliverSMRespQueue.take();
							if (rep == null)
								continue;

							boolean isOK;
							try {
								isOK = SMS_StatusReport.deliverSMStatusReport(config, rep);
								logger.info("Report delivery status success: " + rep.isdn);
							} catch (Exception e) {
								isOK = false;
								logger.error("Report delivery status failed: {}, {}", rep.isdn, isOK, e);
							}

							/*if (!isOK && config.SmsTracker_NON_STOP_RETRY_IF_FAIL == 1)
								deliverSMRespQueue.put(rep);*/
						} catch (Exception e) {
							logger.error("Delivery Reporter: ", e);
						}
					}
				}
			}));
		}

		// receiver message thread
		for (int i = 1; i <= 2; i++) {

			threadList.add(new Thread(new Runnable() {

				@Override
				public void run() {
					while (RUN) {
						try {
							SmsDeliverSMWrapper rec = ConcurrentQueue.receivedSMS.take();
							if (rec == null)
								continue;

							// Update send date to redis
							rec.sendDate = new Date();

							boolean isOK = false;
							try {
								isOK = ReceiveSMS.receiveSMSHandle(config, rec);
							} catch (Exception ex) {
								logger.error("DeliverSM from customer failed: " + rec.isdn, ex);
							}

							if (isOK) {
								logger.info("DeliverSM from customer Success: " + rec.isdn);
								existedReceiveSMS.remove(rec.GUID);
							} else {
								ConcurrentQueue.receivedSMS.put(rec);
							}
						} catch (Exception e) {
							logger.error("Customer Reply Reporter: ", e);
						}
					}
				}
			}));
		}

		RUN = true;

		// start thread list
		Iterator<Thread> t = threadList.iterator();
		while (t.hasNext()) {
			t.next().start();
		}
	}

    /**
     * destroy service reporter
     *
     */
    @Override
    public void destroy() {

    	RUN = false;

		Iterator<Thread> t = threadList.iterator();
		while (t.hasNext()) {
			Thread tmp = t.next();
			try {
				if (tmp != null && tmp.isAlive()) {
					tmp.interrupt();
				}
			} catch (Exception ex) {
				logger.error("error", ex);
			}
		}
	}

	@Override
	public void reportSubmitStatus(SmsSubmitResp report) {
		try {
			submitSMRespQueue.put(report);
		} catch (InterruptedException e) {
			logger.error("ReportSubmit Error: ", e);
		}
	}

	@Override
	public void reportDeliverStatus(SmsDeliverSMResp report) {
		try {
			deliverSMRespQueue.put(report);
		} catch (InterruptedException e) {
			logger.error("ReportDeliver Error: ", e);
		}
	}

	@Override
	public void reportReceivedSMS(DeliverSM sm, String content) {

		if (sm == null)
			return;

		SmsDeliverSMWrapper wr = new SmsDeliverSMWrapper();
		wr.isdn = sm.getSourceAddr().getAddress();
		wr.content = content;
		wr.GUID = java.util.UUID.randomUUID().toString();
		wr.sendDate = new Date();

		existedReceiveSMS.add(wr.GUID);

		try {
			ConcurrentQueue.receivedSMS.put(wr);
		} catch (InterruptedException e) {
			logger.error("ReportReceivedSM Error: ", e);
		}
	}

	@Override
	public void reportReceivedSMS(SmsDeliverSMWrapper wr) {
		if (wr == null)
			return;

		if (!existedReceiveSMS.contains(wr.GUID)) {
			existedReceiveSMS.add(wr.GUID);

			try {
				ConcurrentQueue.receivedSMS.put(wr);
			} catch (InterruptedException e) {
				logger.error("ReportReceivedSM Error: ", e);
			}
		}
	}
}
