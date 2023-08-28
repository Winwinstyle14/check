package com.vhc.ec.auth.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Component
public class GlobalValue {

    public static String SECRECT_KEY_DIRECTORY;

    @Value("${vhc.ec.jwt.secrect_key_directory}")
    public void setSecrectKeyDirectoryStatic(String secrectKeyDirectory) {
        SECRECT_KEY_DIRECTORY = secrectKeyDirectory;
    }

}
