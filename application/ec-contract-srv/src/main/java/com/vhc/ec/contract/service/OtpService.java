package com.vhc.ec.contract.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vhc.ec.contract.dto.UserAttemptOtp;
import com.vhc.ec.contract.util.GenerateCodeUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private static final Integer EXPIRE_MINS = 2;

    private LoadingCache<Integer, Integer> otpCache;

    public OtpService(){
        super();
        otpCache = CacheBuilder.newBuilder().
                expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    public Integer load(Integer key) {
                        return 0;
                    }
                });
    }

    public int generateOTP(Integer key){
        int otp = GenerateCodeUtil.generateCode();
        otpCache.put(key, otp);
        return otp;
    }

    public int getOtp(Integer key){
        try{
            return otpCache.get(key);
        } catch (Exception e){
            return 0;
        }
    }

    //This method is used to clear the OTP catched already
    public void clearOTP(String key){
        otpCache.invalidate(key);
    }
}
