package com.vhc.ec.notification.smpp;

import com.vhc.ec.notification.config.SmppConfig;
import com.vhc.ec.notification.entity.Sms;
import com.vhc.ec.notification.smpp.dto.SmsSubmitWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class SmppProxyManager {

	private HashMap<String, SmppProxy> proxies = new HashMap<String, SmppProxy>();
	private SmppConfig sc;

	public void addProxyFromConfig(SmppConfig sc, SmsReporter reporter) throws IOException {
		SmppProxy sp = new SmppProxy(sc.smppId, sc, sc.numSessions, reporter);
		proxies.put(sc.smppId, sp);

		if (this.sc == null) {
			this.sc = sc;
		}
	}

	public void startAllProxies() {
		if (proxies == null || proxies.size() == 0)
			return;

		for (SmppProxy sp : proxies.values()) {
			sp.start();
		}
	}

	/**
	 * Stop all proxies
	 */
	public void stopAllProxies() {
		if (proxies == null || proxies.size() == 0)
			return;

		for (SmppProxy sp : proxies.values()) {
			sp.stop();
		}
	}

	/**
	 * Restart all proxies
	 */
	public void restartAllProxies() {
		if (proxies == null || proxies.size() == 0)
			return;

		for (SmppProxy sp : proxies.values()) {
			sp.restart();
		}
	}

	/**
	 * Destroy all proxies
	 */
	public void destroyAllProxies() {
		if (proxies == null || proxies.size() == 0)
			return;

		for (SmppProxy sp : proxies.values()) {
			sp.destroy();
		}
	}

	public boolean existBoundedSession() {
		for (SmppProxy sp : proxies.values())
			if (sp.existBoundedSession())
				return true;

		return false;
	}

	/**
	 * Send smpp over proxy
	 *
	 * @param smppId
	 *            proxy/smpp id
	 * @param sm
	 *            SubmitSM Object
	 * @return 1 if Submit request is put to Queue for later processing. 0 if
	 *         not
	 * @throws InterruptedException
	 */
	public int send(String smppId, SmsSubmitWrapper sm) throws InterruptedException {
		if (!proxies.containsKey(smppId))
			return -1;

		return proxies.get(smppId).send(sm);
	}

	public int send(Sms sms) throws InterruptedException {

		if (proxies.size() == 0)
			return -1;

		try {
			SmsSubmitWrapper sw = new SmsSubmitWrapper();
			//Sms sms = mapper.readValue(record.value(), Sms.class);

			sms.setSeq(InMemorySMS.getNewSequenceNumber());
			sw.sms = sms;

			return send(this.sc.smppId, sw);
		} catch (Exception e) {
			log.error("error: sms={}", sms, e);
		}

		return -2;
	}
}
