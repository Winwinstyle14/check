package com.vhc.ec.api.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Thông tin cấu hình của khách hàng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "vhc.ec.auth.customer")
public class CustomerAuthProperties {

    private String tokenUrl;

    private String[] permitUrls;

}
