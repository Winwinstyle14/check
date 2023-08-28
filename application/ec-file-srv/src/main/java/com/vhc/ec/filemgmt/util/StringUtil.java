package com.vhc.ec.filemgmt.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class StringUtil { 
    
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static void main(String[] args) throws IOException {
    	String filePath = "C:\\Users\\VHC_TUANANH\\OneDrive\\Desktop\\";
    	String originalFileName = "official_contract_bct_signed.pdf";

    	byte[] input_file = Files.readAllBytes(Paths.get(filePath+originalFileName));

    	byte[] encodedBytes = Base64.getEncoder().encode(input_file);
    	// bytes to hex

    	String hexEncodedBytes = bytesToHex(encodedBytes);

    	System.out.println(hexEncodedBytes);
    }
}
