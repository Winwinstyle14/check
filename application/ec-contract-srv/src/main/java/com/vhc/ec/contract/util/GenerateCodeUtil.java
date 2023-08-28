package com.vhc.ec.contract.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
public final class GenerateCodeUtil {
    public static int generateCode() {
        int code = 0;

        try {
            var random = new Random();
            code = random.nextInt(900000) + 100000; // 6-digit
        } catch (Exception e) {
            log.error("error generate code: {}", e);
        }

        return code;
    }
}
