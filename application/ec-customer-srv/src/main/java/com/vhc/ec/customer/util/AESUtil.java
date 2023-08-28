package com.vhc.ec.customer.util;

import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.Arrays;

/**
 * https://teamvietdev.com/ma-hoa-va-giai-ma-aes-trong-java/
 * @author VHC_TUANANH
 *
 */
public class AESUtil { 
    private static final String _KEY = "eContract@dmin";
    
	public static String encrypt(String strToEncrypt) { 
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] key = _KEY.getBytes("UTF-8");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println(e.toString());
		} 
		
		return null;
	}
	
	public static String decrypt(String strToDecrypt) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] key = _KEY.getBytes("UTF-8");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		String input = "123456"; 
	    
	    String cipherText = AESUtil.encrypt( input );
	    
	    System.out.println("cipherText="+cipherText);
	    
	    String plainText = AESUtil.decrypt("HC89H19dL/mXfBxToYdqhQ==");
	    
	    System.out.println("plainText="+plainText);
	}
}
