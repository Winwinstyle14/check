package com.vhc.ec.notification.smpp;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.vhc.ec.notification.entity.Sms;

public class InMemorySMS {

	private static Object lock = new Object();

	/**
	 * SMS which not submit to SMSC doesn't have any ID. Using sequence number
	 * is the only choice to keep track which SMS sent.
	 */
	private static int SMSSequenceNumberCounter = 0;
	private static int seed = 0;
	private static int mod = 1;

	/**
	 * Keep track of SMS by its sequence number
	 */
	private static Cache<Integer, Sms> inMem = CacheBuilder.newBuilder().maximumSize(50000000)
			.expireAfterWrite(20, TimeUnit.MINUTES).build();

	/**
	 * Keep track Send/SubmitSM time
	 */
	private static Cache<Integer, Long> timeTrack = CacheBuilder.newBuilder().maximumSize(50000000)
			.expireAfterWrite(20, TimeUnit.MINUTES).build();

	/**
	 * When submitted to SMSC, SMS received a new ID by SMSC to keep track of
	 * its delivery status.
	 */
	private static Cache<String, Integer> _map_between_messageid_seq = CacheBuilder.newBuilder().maximumSize(50000000)
			.expireAfterWrite(20, TimeUnit.MINUTES).build();


	/**
	 * Add time track to a sequence number of SMS
	 *
	 * @param sequenceNumber
	 * @param time
	 */
	public static void addTimeTrack(int sequenceNumber, long time) {
		timeTrack.put(sequenceNumber, time);
	}

	/**
	 * Get time tracked
	 *
	 * @param sequenceNumber
	 * @return
	 */
	public static Long getTimeTrack(int sequenceNumber) {
		try {
			return timeTrack.get(sequenceNumber, new Callable<Long>() {
				@Override
				public Long call() {
					return null;
				}
			});
		} catch (ExecutionException e) {
			return null;
		}
	}

	/**
	 * Remove time tracked
	 *
	 * @param sequenceNumber
	 */
	public static void removeTimeTrack(int sequenceNumber) {
		timeTrack.invalidate(sequenceNumber);
	}

	/**
	 * Get new Sequence Number for marking/tracking SMS
	 *
	 * @return
	 */
	public static int getNewSequenceNumber() {
		synchronized (lock) {
			SMSSequenceNumberCounter += mod;
			if (SMSSequenceNumberCounter > 200000000)
				SMSSequenceNumberCounter = seed;

			return SMSSequenceNumberCounter;
		}
	}

	public static void initSeqNumber(int seed, int mod) {
		InMemorySMS.seed = seed;
		InMemorySMS.mod = mod;

		SMSSequenceNumberCounter = seed;
	}

	public static void add(int sequenceNumber, Sms obj) {
		inMem.put(sequenceNumber, obj);
	}

	public static Sms get(int sequenceNumber) {
		try {
			return inMem.get(sequenceNumber, new Callable<Sms>() {
				@Override
				public Sms call() {
					return null;
				}
			});
		} catch (Exception e) {
			return null;
		}
	}

	public static void remove(long sequenceNumber) {
		inMem.invalidate(sequenceNumber);
	}

	/**
	 * Set map between sms's sequencenumber and messageID
	 */
	public static void cacheSeqMessageId(int sequenceNumber, String messageID) {
		_map_between_messageid_seq.put(messageID, sequenceNumber);
	}

	/**
	 * Get sequence number by messageId
	 *
	 * @param messageId
	 * @return
	 */
	public static Integer getSeqByMessageId(String messageId) {
		try {
			return _map_between_messageid_seq.get(messageId, new Callable<Integer>() {
				@Override
				public Integer call() {
					return null;
				}
			});
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Remove MessageID
	 *
	 * @param smsc_smsId
	 */
	public static void removeCacheMessageId(String smsc_smsId) {
		_map_between_messageid_seq.invalidate(smsc_smsId);
	}
}
