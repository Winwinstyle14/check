package com.vhc.ec.notification.util;

import org.smpp.util.ByteBuffer;

import com.google.common.base.Charsets;

public class ByteUtil {

	private static int seqOfMsg = 0;
    private static int seqOfMsg16 = 0;

	public static byte[] getLongText16bit(String text, int ref) {
		/* Gui concated SMS refnumber = 16bit */
		int msgLength = text.length();
		int nNumberOfMsg = msgLength % 65 == 0 ? msgLength / 65 : (msgLength / 65) + 1; // 65 char in each sms

		/*
		 * Fix 03/10/2013 Dong bo seqOfMsg Chuyen sang 16bit
		 */
		ref = (ref > 65535) ? 1 : ref;

		byte data[] = text.getBytes(Charsets.UTF_16);
		msgLength = data.length;

		int maxByteData = 132; // 139 - 7

		int index = 2; // Omit first 2 byte indicate BOM of UTF-16

		ByteBuffer buf = new ByteBuffer();
		for (int i = 0; i < nNumberOfMsg; i++) {
			buf.appendByte((byte) 0x06);
			buf.appendByte((byte) 0x08);
			buf.appendByte((byte) 0x04);
			buf.appendShort((short) ref);
			buf.appendByte((byte) nNumberOfMsg); // number piece
			buf.appendByte((byte) (i + 1)); // index of piece

			int availableData = msgLength - index;
			int byteData = availableData > maxByteData ? maxByteData : availableData;
			byte b[] = new byte[byteData];
			System.arraycopy(data, index, b, 0, byteData);

			index += byteData;
			buf.appendBytes(b);
		}
		return buf.getBuffer();
	}

	public static byte[] getLongText8bit(byte[] data, int ref) {
		/* Sent ref number 8bit */

		int nNumberOfMsg = 1;
		int msgLength = data.length;
		/**
		 * if msgLength >133 then Header is 12 bytes include 7 bytes UDH and 5
		 * bytes separater partern. But the first msg has 13 byte header include
		 * 01 byte 0x30 (version) Message is devided to many which one has 128
		 * byte data
		 */

		/*
		 * Fix 03/10/2013 Dong bo seqOfMsg Chuyen sang 16bit
		 */
		// nNumberOfMsg = (msgLength - 1) / 133 + 1;
		nNumberOfMsg = (msgLength - 1) / 134 + 1;
		ByteBuffer buf = new ByteBuffer();
		int maxByteData;
		int index = 0;
		msgLength = data.length;

		ref = (ref > 255) ? 1 : ref;

		for (int i = 0; i < nNumberOfMsg; i++) {
			maxByteData = 140;
			buf.appendByte((byte) 0x05);
			buf.appendByte((byte) 0x00);
			buf.appendByte((byte) 0x03);
			buf.appendByte((byte) ref);
			buf.appendByte((byte) nNumberOfMsg);
			buf.appendByte((byte) (i + 1));
			maxByteData -= 6;
			int availableData = msgLength - index;
			int byteData = availableData > maxByteData ? maxByteData : availableData;
			byte b[] = new byte[byteData];
			System.arraycopy(data, index, b, 0, byteData);

			index += byteData;
			buf.appendBytes(b);

		}
		return buf.getBuffer();
	}

	public static byte[] getLongText(String text) {

		boolean is16Bit = StringUtil.containUnicodeChar(text);

		if (!is16Bit) {
			if (++seqOfMsg >= 255) {
				seqOfMsg = 1;
			}
		} else {
			if (++seqOfMsg16 >= 0xFFFF) {
				seqOfMsg16 = 1;
			}
		}

		return is16Bit ? getLongText16bit(text, seqOfMsg16) : getLongText8bit(text.getBytes(), seqOfMsg);
	}
}
