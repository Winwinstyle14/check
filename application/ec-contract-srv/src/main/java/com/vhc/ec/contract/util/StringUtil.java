package com.vhc.ec.contract.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.lang.RandomStringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtil {

    private static final int PASSWORD_RANDOM_LENGTH = 6;

    /**
     * Tự sinh mật khẩu với độ dài cố định
     *
     * @param length Độ dài cố định của mật khẩu
     * @return Mật khẩu mặc định
     */
    public static String generatePwd(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    public static String generatePwd() {
        return generatePwd(PASSWORD_RANDOM_LENGTH);
    }
    
    public static String generateMessageId(String senderId) {
        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYMM");
        String dateAsString = dateObj.format(formatter);
        return senderId + dateAsString + UUID.randomUUID().toString().toUpperCase(Locale.ROOT).replace("-", "");
    }
    
    public static Optional<String> getMSTFromCert(String certB64) { 
    	try {
    		byte encodedCert[] = Base64.getDecoder().decode(certB64);
            ByteArrayInputStream inputStream  =  new ByteArrayInputStream(encodedCert);

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(inputStream);
            
            String dn = cert.getSubjectX500Principal().getName();
            
            LdapName ldapDN = new LdapName(dn);
            for(Rdn rdn: ldapDN.getRdns()) {
            	if(rdn.getType().equals("UID")) { 
            		return Optional.of(rdn.getValue().toString());
            	}
            }
		} catch (Exception e) {
			log.error("Can't get UID from Certificate: ", e);
		} 
    	
    	return Optional.empty();
    }

    public static String base64ToHex(String base64) {
        String rs = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            final char[] hexArray = "0123456789abcdef".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0, v; j < bytes.length; j++) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            rs = new String(hexChars);
        }catch (Exception e){
            log.error("Can't convert base64 to hex: ", e);
        }

        return rs;
    }
}
