package com.vhc.ec.api.auth;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu cho ứng dụng biết cần kiểm tra xác thực của khách hàng.
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomerAutoConfiguration.class)
public @interface EnableCustomerAuth {
}
