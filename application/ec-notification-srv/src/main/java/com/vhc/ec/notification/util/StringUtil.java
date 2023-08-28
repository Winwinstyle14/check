package com.vhc.ec.notification.util;

import org.apache.commons.lang.RandomStringUtils;

import com.google.common.base.CharMatcher;

public class StringUtil {

	public static boolean containUnicodeChar(String st) {

		return !CharMatcher.ASCII.matchesAllOf(st);
	}

	public static String utf16_to_utf8(String st) {

		String out = null;
		try {
			out = new String(st.getBytes("UTF-8"), "ISO-8859-1");
		} catch (java.io.UnsupportedEncodingException e) {
			return "";
		}
		return out;
	}
	
	private static final int PASSWORD_RANDOM_LENGTH = 8;

    /**
     * Tự sinh mật khẩu với độ dài cố định
     *
     * @param length Độ dài cố định của mật khẩu
     * @return Mật khẩu mặc định
     */
    public static String generateLinkCode(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    public static String generateLinkCode() {
        return generateLinkCode(PASSWORD_RANDOM_LENGTH);
    }
}
