package com.vhc.ec.api.versioning;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Open multi-version API control
 *
 * @author VHC JSC
 * @version 1.0
 * @see ApiVersionProperties configuration properties
 * @see ApiVersionAutoConfiguration configuration class
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ApiVersionAutoConfiguration.class)
public @interface EnableApiVersioning {
}
