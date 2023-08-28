package com.vhc.ec.notification.smpp;

import java.util.Vector;

import com.vhc.ec.notification.config.SmppConfig;
import com.vhc.ec.notification.smpp.dto.SmsSubmitWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SmppProxy {

	protected long startTime;

	protected SmppConfig config;

	protected Vector<SessionUnit> sessions;

	private String id;

	private Object lock = new Object();
	private Logger logger;

	public SmppProxy(String smppid, SmppConfig config, int sessionCount, SmsReporter reporter) {

		this.id = smppid;
		this.config = config;

		this.sessions = new Vector<SessionUnit>();
		logger = LoggerFactory.getLogger(SmppProxy.class);

		for (int i = 0; i < sessionCount; i++) {
			sessions.add(new SessionUnit(new SmppSession(this, smppid, config, reporter)));
		}
	}

	public void start() {
		this.startTime = System.currentTimeMillis();
		synchronized (this.lock) {
			for (SessionUnit u : this.sessions) {
				u.session.bind();
				u.session_t.start();
			}
		}
	}

	public void stop() {
		synchronized (this.lock) {
			for (SessionUnit u : this.sessions) {
				u.session.unbind();
			}
		}
	}

	public void restart() {
		stop();

		synchronized (this.lock) {
			for (SessionUnit u : this.sessions) {
				u.session.bind();
			}
		}
	}

	/**
	 * Destroy proxy and its sessions/threads
	 */
	public void destroy() {
		synchronized (this.lock) {
			if (this.sessions == null)
				return;

			for (SessionUnit u : this.sessions) {
				u.session.destroy();
			}

			for (SessionUnit u : this.sessions) {
				try {
					u.session_t.join(10000);
					if (u.session_t.isAlive())
						u.session_t.interrupt();
				} catch (InterruptedException e) {
					if (u.session_t.isAlive())
						u.session_t.interrupt();
				}
			}
		}
	}

	/**
	 * Check if any session is bound
	 *
	 * @return true if exists a session which is bound
	 */
	public boolean available() {
		for (SessionUnit u : this.sessions) {
			if (u.session.bound) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get smppID
	 *
	 * @return
	 */
	public String getSmppId() {
		return id;
	}

	/**
	 * Put SubmitSm to Session for processing later on.
	 *
	 * @param smRequest
	 * @return 1 if this connection is bound or 0 if not
	 * @throws InterruptedException
	 */
	public int send(SmsSubmitWrapper smRequest) throws InterruptedException {
		SmppSession session = getSession();
		if (session != null) {
			// this.totalSubmit = CounterUtils.IncreaseCounter(this.totalSubmit, 1L);
			return session.send(smRequest);
		}

		if (this.sessions.size() != 0) {
			this.logger.warn("no available smpp session, user default");
			session = this.sessions.elementAt(0).session;
			return session.send(smRequest);
		}

		this.logger.warn("no session, can't send SmsData");
		return 0;
	}

	public boolean existBoundedSession() {
		for (SessionUnit u : this.sessions)
			if (u.session.isConnectionAvailable()) {
				return true;
			}

		return false;
	}

	/**
	 * Get minimal submited sm sessions.
	 *
	 * @return
	 */
	private SmppSession getSession() {
		long min = Long.MAX_VALUE;
		SmppSession theBestSession = null;

		for (SessionUnit u : this.sessions) {
			long sub = u.session.getTotalSubmits();
			if ((sub < min) && (u.session.bound)) {
				min = sub;
				theBestSession = u.session;
			}
		}

		if (theBestSession == null) {
			for (SessionUnit u : this.sessions) {
				long sub = u.session.getTotalSubmits();
				if ((sub < min)) {
					min = sub;
					theBestSession = u.session;
				}
			}
		}

		return theBestSession;
	}

	class SessionUnit {

        public SmppSession session;
        public Thread session_t;

        public SessionUnit(SmppSession session) {
            this.session = session;
            this.session_t = new Thread(session);
        }
    }
}
