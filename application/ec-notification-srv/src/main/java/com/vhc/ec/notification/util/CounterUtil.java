package com.vhc.ec.notification.util;

public class CounterUtil {

	public static long increaseCounter(long a, long d) {
		a += d;
		return a > 2000000000L ? 0 : a;
	}
}
