package com.vhc.ec.notification.smpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpp.Data;
import org.smpp.ServerPDUEvent;
import org.smpp.ServerPDUEventListener;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.WrongSessionStateException;
import org.smpp.pdu.Address;
import org.smpp.pdu.AddressRange;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransciever;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.EnquireLink;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.Unbind;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.util.ByteBuffer;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import com.vhc.ec.notification.config.SmppConfig;
import com.vhc.ec.notification.entity.Sms;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMResp;
import com.vhc.ec.notification.smpp.dto.SmsSubmitResp;
import com.vhc.ec.notification.smpp.dto.SmsSubmitWrapper;
import com.vhc.ec.notification.util.ByteUtil;
import com.vhc.ec.notification.util.CounterUtil;
import com.vhc.ec.notification.util.SmsErrorParser;
import com.vhc.ec.notification.util.StringUtil;

public class SmppSession implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(SmppSession.class);

	public String connectedTime;
	public boolean bound = false;
	//private int enquireLinks;
	private Queue<ExpirePreventDDOS> queueExpirePreventDDOS = new ConcurrentLinkedQueue<>();
	private Map<Long, Integer> mapPreventDDOS = new ConcurrentHashMap<>();

	private SmppConfig config;
	private Session sessionTransceiver = null;
	private BlockingQueue<SmsSubmitWrapper> needToSendSms = new LinkedBlockingQueue<SmsSubmitWrapper>();
    //private boolean firstBind;
	private long submits;
    private Object lock = new Object();
    private EnquireThread enquireThread = new EnquireThread();
    /**
     * Mark if this sessionTransceiver should stop
     */
    private boolean needToStop = false;
    /**
     * Time when this sessionTransceiver object created
     */
    //private long startTime;
    /**
     * Time of last-sending SMS
     */
	//private long lastSend;
    /**
     * SMS Status reporter
     */
	private SmsReporter reporter;

    public SmppSession(SmppProxy parentProxy, String smppid, SmppConfig config, SmsReporter reporter) {
        //this.parent = parentProxy;
        //this.smppId = smppid;
        this.config = config;
        //this.firstBind = true;
        //this.startTime = System.currentTimeMillis();
        this.reporter = reporter;

        Thread t = new Thread(new PreventDDOS(this.queueExpirePreventDDOS, this.mapPreventDDOS));
        t.start();
    }

    /**
     * bind smpp connection
     */
	public void bind() {

		synchronized (this.lock) {
			try {
				//this.enquireLinks = 0;

				if (this.bound)
					return;
				else {
					BindRequest request;
					request = new BindTransciever();

					TCPIPConnection connection = new TCPIPConnection(this.config.ip, this.config.port);
					connection.setReceiveTimeout(-1);

					this.sessionTransceiver = new Session(connection);
					this.sessionTransceiver.enableStateChecking();

					request.setSystemId(this.config.systemId);
					request.setPassword(this.config.password);
					// request.setSystemType("CMT");
					request.setSystemType(this.config.systemType);
					// request.setInterfaceVersion(this.config.interfaceVerion);

					AddressRange addr = new AddressRange();
					addr.setTon((byte) 0x05);
					addr.setNpi((byte) 0);
					addr.setAddressRange(this.config.addressRange);
					request.setAddressRange(addr);

					// receiver sms listener
					BindResponse response = this.sessionTransceiver.bind(request, new SmppListener());

					this.sessionTransceiver.getReceiver().setReceiveTimeout(Data.RECEIVER_TIMEOUT);
					this.sessionTransceiver.getReceiver().setQueueWaitTimeout(-1);

					if (response == null) {
						this.bound = false;
					} else {
						if (response.getCommandStatus() == 0) {
							this.bound = true;
							this.connectedTime = new Date().toString();
						} else {
							this.bound = false;
						}
					}

					logger.info("Binding status: " + this.bound);
				}
			} catch (WrongSessionStateException ex) {
				logger.error("error, config={}", this.config, ex);
				this.bound = false;
			} catch (Exception ex) {
				logger.error("error, config={}", this.config, ex);
			} finally {
				//this.firstBind = false;
			}
		}
	}

	public void unbind() {

		logger.info("Unbinding connection to Gateway..");
		synchronized (this.lock) {
			try {
				if (this.bound) {
					if (this.sessionTransceiver != null) {
						this.sessionTransceiver.unbind();
						this.sessionTransceiver.close();
					}
				}
			} catch (Exception ex) {
				logger.error("error", ex);
			}

			this.bound = false;
		}
		logger.info("Unbind connection to Gateway success");
	}

	public void rebind() {
		unbind();
		bind();
	}

	public void destroy() {
		synchronized (this.lock) {
			this.needToStop = true;
			if (this.enquireThread.isAlive())
				this.enquireThread.interrupt();
		}
	}


	public void run() {
		this.enquireThread = new EnquireThread();
		this.enquireThread.start();

		synchronized (this.lock) {
			this.needToStop = false;
		}

		while (true) {
			synchronized (this.lock) {
				if (this.needToStop) {
					unbind();
					if (this.enquireThread.isAlive())
						this.enquireThread.interrupt();

					return;
				}
			}

			int runTime = this.needToSendSms.size();
			int count = 0;

			long startTime = System.currentTimeMillis();
			while (count++ < runTime) {
				// get and remove from queue
				SmsSubmitWrapper smRes = (SmsSubmitWrapper) this.needToSendSms.poll();
				if (smRes == null)
					continue;

//				if (config.popSMSEveryMS > 0) {
//					try {
//						Thread.sleep(config.popSMSEveryMS);
//					} catch (InterruptedException e) {
//						logger.error("SmppSession Thread sleep error: ", e);
//					}
//				}

				// process it
				int res = process(smRes);

				// fail
				if (res == 0) {
					try {
						logger.error(" Send to GW fail: " + smRes.submitSM.getData().getHexDump());
					} catch (ValueNotSetException e) {
						e.printStackTrace();
					}

					if (this.reporter != null) {
						SmsSubmitResp report = new SmsSubmitResp();
						report.smsId = "" + smRes.sms.getId();
						report.commandStatus = this.bound ? 1000 : -1; // Unknown
																		// error
						report.isSubmitSuccess = false;
						report.error = SmsErrorParser.GetErrStringByCommandStatus(report.commandStatus);

						// close
						SmppSession.this.reporter.reportSubmitStatus(report);
						/*if (!smRes.sms.IsOTP())
							SmppSession.this.reporter.ReportSubmitStatus(report);
						else
							SmppSession.this.reporter.ReportOTPSubmitStatus(report);*/

					}
				}
			}
			long endTime = System.currentTimeMillis();

			long totalTime = endTime - startTime;
			double totalTimeInSec = 1.0 * totalTime / 1000;

			if (runTime > 0) {
				logger.info(String.format(" Send a batch [%d] sms in [%.2f] sec => Speed [%.2f] sms/sec", runTime,
							totalTimeInSec, runTime / totalTimeInSec));
			}

			if (totalTime < 1000) {
				try {
					Thread.sleep(1000 - totalTime);
				} catch (InterruptedException e) {
					logger.error(" SmppSession thread sleep error: ", e);
				}
			}
		}
	}

	public boolean isConnectionAvailable() {
		return this.sessionTransceiver != null && this.sessionTransceiver.isBound()
				&& this.sessionTransceiver.isOpened() && this.sessionTransceiver.getConnection().isOpened();
	}

	/**
	 * Process a SubmitSM
	 *
	 * @param smRequest
	 * @return 0 if fail or 1 if success. If mode is async then this method will
	 *         return 1 and not wait for response from smsc
	 */
	public int process(SmsSubmitWrapper smRequest) {
		//this.lastSend = System.currentTimeMillis();
		return submit(smRequest);
	}

	/**
	 * Put SubmitSm to Queue for processing later on.
	 *
	 * @param smRequest
	 * @return 1 if this connection is bound or 0 if not
	 * @throws InterruptedException
	 */
	public int send(SmsSubmitWrapper smRequest) throws InterruptedException {
		if (this.bound) {
			this.needToSendSms.put(smRequest);
			return 1;
		}

		return 0;
	}

	private SubmitSM createSubmitForConcatinated(String sender, String receiver, boolean IsUnicode)
			throws WrongLengthOfStringException {
		SubmitSM submit = new SubmitSM();

		Address sourceAddr = new Address(sender);
		sourceAddr.setTon(this.config.sourceTone);
		sourceAddr.setNpi(this.config.sourceNpi);
		Address desAddr = new Address(receiver);
		desAddr.setTon(this.config.destinationTone);
		desAddr.setNpi(this.config.destinationNpi);
		submit.setSourceAddr(sourceAddr);
		submit.setDestAddr(desAddr);

		submit.setProtocolId((byte) 0);

		submit.setRegisteredDelivery((byte) 1);
		submit.setReplaceIfPresentFlag((byte) 0);
		submit.setEsmClass((byte) 0);
		submit.setDataCoding(IsUnicode ? (byte) (0x08) : (byte) 0);

		// submit.setPriorityFlag((byte) 3);
		// submit.setSmDefaultMsgId((byte) 0);
		// submit.setUserMessageReference((byte) 23);

		return submit;
	}

	/**
	 * Submit message over connection to smsc
	 *
	 * @param request
	 * @return 0 if fail or 1 if success. If mode is async then this method will
	 *         return 1 and not wait for response from smsc
	 */
	private int submit(SmsSubmitWrapper request) {
		if (!this.bound || this.sessionTransceiver == null) {
			logger.error("Not bound to Gateway cannot submit.");
			return 0;
		}

		try {
			if (!this.sessionTransceiver.isBound() || !this.sessionTransceiver.isOpened()) {
				this.rebind();
			}

			if (!this.sessionTransceiver.isBound() || !this.sessionTransceiver.isOpened()) {
				logger.error("Not bound to Gateway cannot submit.");
				return 0;
			}

			try {
				Long key = Hashing.sipHash24()
								.hashBytes((request.sms.getPhone() + "$" + request.sms.getId()).getBytes())
								.asLong();

				Integer check = this.mapPreventDDOS.get(key);

				// There is no map for it
				if (check == null) {
					int quotaLimit = 20;

					this.mapPreventDDOS.put(key, new Integer(quotaLimit));

					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.HOUR, 24);
					this.queueExpirePreventDDOS.add(new ExpirePreventDDOS(cal.getTime(), key));
				} else {
					int val = check.intValue();
					if (val <= 0) {
						return 0;
					}
					this.mapPreventDDOS.replace(key, new Integer(--val));

					// System.out.println(val);
				}
			} catch (Exception ex) {
				logger.error("error", ex);
				return 0;
			}

			int result = 1;

			logger.info("Debug sending: " + request.sms.getPhone() + " "
							+ request.sms.getContent() + " " + request.submitSM.getData().getHexDump());

			// For short message
			boolean isContentUnicode = StringUtil.containUnicodeChar(request.sms.getContent());
			if ((request.sms.getContent().length() <= 160 && !isContentUnicode)
					|| (request.sms.getContent().length() <= 70 && isContentUnicode)) {

				String contentSend = "" + request.sms.getContent();
				if (StringUtil.containUnicodeChar(contentSend)) {
					request.submitSM.setDataCoding((byte) (0x08)); // UCS-2
					try {
						request.submitSM.setShortMessage(contentSend, Charsets.UTF_16.name());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					contentSend = "" + contentSend;
					request.submitSM.setDataCoding((byte) 0);
					request.submitSM.setShortMessage(contentSend);
				}
				this.sessionTransceiver.submit(request.submitSM);
			} else {
				int s = 0;
				int e = 0;
				boolean firstSMSInSeq = true;
				// int totalSMS = 0;

				// For long message
				byte[] content = ByteUtil.getLongText(request.sms.getContent());

				int trunkLength = isContentUnicode ? 139 : 140;
				while (e < content.length) {
					e = s + trunkLength < content.length ? s + trunkLength : content.length;
					byte[] smsdata = new byte[e - s];

					System.arraycopy(content, s, smsdata, 0, e - s);

					try {
						SubmitSM submit = createSubmitForConcatinated(this.config.addressRange, request.sms.getPhone(),
								isContentUnicode);
						submit.setShortMessageData(new ByteBuffer(smsdata));
						submit.setEsmClass((byte) 64);
						// totalSMS++;

						if (firstSMSInSeq)
							submit.setSequenceNumber(request.submitSM.getSequenceNumber());
						else
							submit.setSequenceNumber(InMemorySMS.getNewSequenceNumber());

						firstSMSInSeq = false;

						this.sessionTransceiver.submit(submit);
					} catch (Exception ex) {
						logger.error("error", ex);
						result = 0;
					}
					s = e;
				}
			}

			synchronized (this.lock) {
				this.submits = CounterUtil.increaseCounter(this.submits, 1L);
			}

			return result;
		} catch (Exception ex) {

			logger.error("error", ex);

			if (!this.bound || !this.sessionTransceiver.getConnection().isOpened())
				this.rebind();

			try {
				this.sessionTransceiver.submit(request.submitSM);

				synchronized (this.lock) {
					this.submits = CounterUtil.increaseCounter(this.submits, 1L);
				}

				return 1;
			} catch (Exception e) {
				logger.error("error", ex);
			}
		}

		return 0;
	}

	/**
	 * Get total submit request to smsc
	 *
	 * @return number of submit requested to smsc
	 */
	public long getTotalSubmits() {
		synchronized (this.lock) {
			return this.submits;
		}
	}

    class ExpirePreventDDOS {
		public Date expire;
		public Long key;

		public ExpirePreventDDOS(Date exp, Long k) {
			this.expire = exp;
			this.key = k;
		}
	}

	class PreventDDOS implements Runnable {
		private Queue<ExpirePreventDDOS> queue_expirePreventDDOS;
		private Map<Long, Integer> map_preventDDOS;

		public PreventDDOS(Queue<ExpirePreventDDOS> q_expirePreventDDOS, Map<Long, Integer> m_preventDDOS) {
			this.queue_expirePreventDDOS = q_expirePreventDDOS;
			this.map_preventDDOS = m_preventDDOS;
		}

		@Override
		public void run() {
			if (this.queue_expirePreventDDOS == null || this.map_preventDDOS == null)
				return;

			while (true) {
				try {
					ExpirePreventDDOS now = this.queue_expirePreventDDOS.peek();
					if (now == null) { // queue is empty
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						System.gc();
						continue;
					}

					if (now.expire == null || now.expire.before(new Date())) {
						// System.out.println("Remove " + now.key + " " +
						// this.queue_expirePreventDDOS.size() + " " +
						// this.map_preventDDOS.size());

						this.queue_expirePreventDDOS.poll();
						this.map_preventDDOS.remove(now.key);

						// System.out.println(this.queue_expirePreventDDOS.size());
					} else {
						// System.out.println("Soon expire: " + now.expire);

						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						System.gc();
					}
				} catch (Exception ex) {
					logger.error("error", ex);
				}
			}
		}
	}

	/**
	 * EnquireThread make enquire link requests to SMSC
	 */
	class EnquireThread extends Thread {
		private void enquireLink() {
			if (!SmppSession.this.sessionTransceiver.isBound() || !SmppSession.this.sessionTransceiver.isOpened()) {
				SmppSession.this.rebind();
			}

			try {
				if (SmppSession.this.sessionTransceiver != null) {
					EnquireLink request = new EnquireLink();
					SmppSession.this.sessionTransceiver.enquireLink(request);

//					if (config.showHeartBeat == 1 && config.isDebugMode == 1)
//						logger.info(" Send heart beat: " + request.getData().getHexDump());
				}
			} catch (Exception e) {
				SmppSession.this.rebind();
				logger.error("error", e);
			}
		}

		public void run() {
			try {
				for (;;) {
					this.enquireLink();
					sleep(10 * 1000);
				}
			} catch (Exception e) {
				logger.error("Enquirelink error: ", e);
			}
		}
	}

	/**
	 * SmppListener: handle event like connection fail or received response pdu from smsc
	 */
	class SmppListener implements ServerPDUEventListener {
		/**
		 * Process event pdu
		 *
		 * @param pdu
		 */
		protected void process(PDU pdu) throws IOException, WrongSessionStateException, ValueNotSetException {

			if (pdu instanceof DeliverSM) {

				DeliverSM tmp = (DeliverSM) pdu;

				Response response = ((DeliverSM) pdu).getResponse();
				SmppSession.this.sessionTransceiver.respond(response);

				if (tmp.getEsmClass() == 0) { // DeliverSM/Customer Message
					String contentReceived = "";
					if (tmp.getDataCoding() == 0) {
						contentReceived = tmp.getShortMessage();
					} else {
						contentReceived = tmp.getShortMessage(Charsets.UTF_16.name()); // UCS 2 string
						contentReceived = StringUtil.utf16_to_utf8(contentReceived);
					}
					SmppSession.this.reporter.reportReceivedSMS(tmp, contentReceived);
				} else {
					String receiptId;
					if (tmp.hasReceiptedMessageId())
						receiptId = new BigInteger(tmp.getReceiptedMessageId(), 16).toString();
					else {
						String mess = tmp.getShortMessage();
						mess = mess.split(" ")[0]; // get id only
						receiptId = new BigInteger(mess.substring(3), 16).toString();
					}

					Integer seq = InMemorySMS.getSeqByMessageId(receiptId);

					if (seq != null) {
						InMemorySMS.removeCacheMessageId(receiptId);

						Sms fromSMS = InMemorySMS.get(seq);
						if (fromSMS != null) {
							// Remove from InMemory cause we don't need it anymore
							InMemorySMS.remove(seq);
						}

						logger.info("Delivery Report: " + new Date().getTime() + " | " + fromSMS.getPhone());

						//if (fromSMS != null && SmppSession.this.reporter != null && (fromSMS.IsOTP()
						//		|| fromSMS.IsLeadGen() || fromSMS.IsTypeQuestion() || fromSMS.IsTypeGeneral())) {
						if (fromSMS != null && SmppSession.this.reporter != null) {
							SmsDeliverSMResp rep = new SmsDeliverSMResp();
							rep.commandStatus = tmp.getCommandStatus();
							rep.error = SmsErrorParser.GetErrStringByCommandStatus(rep.commandStatus);
							rep.isSent = rep.commandStatus == 0;
							rep.smsId = "" + fromSMS.getId();
							rep.isdn = fromSMS.getPhone();

							SmppSession.this.reporter.reportDeliverStatus(rep);
							/*if (!fromSMS.IsOTP())
								SmppSession.this.reporter.ReportDeliverStatus(rep);
							else
								SmppSession.this.reporter.ReportOTPDeliverStatus(rep);*/
						}
					}
				}
			} else if ((pdu instanceof Request)) {
				Request request = (Request) pdu;
				Response res = request.getResponse();
				res.setSequenceNumber(request.getSequenceNumber());
				SmppSession.this.sessionTransceiver.respond(res);
			} else if ((pdu instanceof EnquireLinkResp)) {
				//logger.info("enquireLinks: " + enquireLinks);
				//SmppSession.this.enquireLinks = 0;
			} else if ((pdu instanceof EnquireLink)) {
				Response response = ((EnquireLink) pdu).getResponse();
				SmppSession.this.sessionTransceiver.respond(response);
			} else if ((pdu instanceof Unbind)) {
				logger.error("Unbinding request");
				SmppSession.this.bound = false;
				Response response = ((Unbind) pdu).getResponse();
				SmppSession.this.sessionTransceiver.respond(response);
				SmppSession.this.sessionTransceiver.close();
				bind();
			} else if (pdu instanceof SubmitMultiSMResp) {
				SubmitMultiSMResp tmp = (SubmitMultiSMResp) pdu;
				logger.error("ABNM GOT MULTI SM RESP: "
						+ tmp.getMessageId() + " " + tmp.getNoUnsuccess() + " " + tmp.debugString());
			}
		}

		public void handleEvent(ServerPDUEvent event) {
			if (event == null)
				return;

			if (event.getConnection() == null) {
				logger.error("Disconnected to gateway");
				SmppSession.this.rebind();
				return;
			}

			PDU pdu = event.getPDU();

			if (pdu != null) {
				if (pdu.getCommandStatus() == SmppConstant.STATUS_THROTTLED)
					logger.error("ABNM Exceed speed error: [0x58] "
									+ SmppConstant.STATUS_MESSAGE_MAP.get(SmppConstant.STATUS_THROTTLED));
				else if (pdu.getCommandStatus() == SmppConstant.STATUS_MSGQFUL)
					logger.error("ABNM Queue full error: [0x14] "
									+ SmppConstant.STATUS_MESSAGE_MAP.get(SmppConstant.STATUS_MSGQFUL));
			}

			try {
				if (pdu instanceof SubmitSMResp) {
					SubmitSMResp tmp = (SubmitSMResp) pdu;

					logger.info(" SubmitSM Resp: "
								+ tmp.getSequenceNumber() + " " + tmp.getMessageId() + " " + tmp.getCommandStatus());

					int seq = tmp.getSequenceNumber();
					Sms mapped = InMemorySMS.get(seq);

					/*Integer Key = new Integer(seq);
					if (SmppSession.this.map_bruno.containsKey(Key)) {
						SmppSession.this.map_bruno.remove(Key);
					}*/

					if (mapped != null) {
						logger.info(mapped.getPhone() + " " + mapped.getContent()
										+ " | Response: " + tmp.getCommandStatus() + " msgId: " + tmp.getMessageId()
										+ " " + tmp.getData().getHexDump());
					}

					if (mapped != null) {
						mapped.setUpdatedAt(new Date());
						if ((tmp.getMessageId() + "").length() != 0) {
							try {
								mapped.setSmscMessageId(new BigInteger(tmp.getMessageId(), 16).toString());

								//InMemorySMS.CacheSeqMessageId(mapped.Seq, mapped.SMSC_MESSAGE_ID);
								InMemorySMS.cacheSeqMessageId(mapped.getSeq(), mapped.getSmscMessageId());
							} catch (Exception ex) {
							}
						}
					}

					/*if (SmppSession.this.reporter != null && mapped != null && (mapped.IsOTP() || mapped.IsLeadGen()
							|| mapped.IsTypeQuestion() || mapped.IsTypeGeneral())) {*/
					if (SmppSession.this.reporter != null && mapped != null) {
						SmsSubmitResp report = new SmsSubmitResp();
						report.smsId = "" + mapped.getId();
						report.isdn = mapped.getPhone();
						report.commandStatus = tmp.getCommandStatus();
						report.isSubmitSuccess = report.commandStatus == 0;
						report.error = SmsErrorParser.GetErrStringByCommandStatus(report.commandStatus);

						// close
						SmppSession.this.reporter.reportSubmitStatus(report);
						/*if (!mapped.IsOTP())
							SmppSession.this.reporter.ReportSubmitStatus(report);
						else
							SmppSession.this.reporter.ReportOTPSubmitStatus(report);*/
					}
				} else
					process(pdu);
			} catch (Exception ex) {
				logger.error("error", ex);
			}
		}
	}
}
