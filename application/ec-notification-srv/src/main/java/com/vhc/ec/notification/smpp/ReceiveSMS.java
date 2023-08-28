package com.vhc.ec.notification.smpp;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.vhc.ec.notification.config.SmppConfig;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveSMS {

	private static Logger logger = LoggerFactory.getLogger(ReceiveSMS.class);

	private static BlockingQueue<Runnable> worksQueue = null;
	private static ThreadPoolExecutor executor = null;

	static {
		try {
			worksQueue = new ArrayBlockingQueue<Runnable>(1000);
			executor = new ThreadPoolExecutor(5, 5 * 2, 10, TimeUnit.SECONDS, worksQueue);

			executor.allowCoreThreadTimeOut(true);

			logger.info("Init ReceiveSMS: poolSize={}, runningTask={}", 5, 1000);
		} catch (Exception e) {
			logger.error("Init ReceiveSMS error", e);
		}
	}

    /**
     * Received SMS
     *
     * @param config
     * @param obj
     * @throws IOException
     */
    public static boolean receiveSMSHandle(SmppConfig config, SmsDeliverSMWrapper obj) throws Exception {

    	logger.info("[receive] " + obj);

    	// TODO: insert SMS MO

    	return true;
    }

    public static void main(String arg[]) throws Exception {

    	SmsDeliverSMWrapper obj = new SmsDeliverSMWrapper();
    	obj.isdn = "84901234567";
    	obj.content = "000";

    	receiveSMSHandle(null, obj);
    }
}
