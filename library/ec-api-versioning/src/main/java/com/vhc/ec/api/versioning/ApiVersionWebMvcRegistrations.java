package com.vhc.ec.api.versioning;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@AllArgsConstructor
public class ApiVersionWebMvcRegistrations implements WebMvcRegistrations {
    @NonNull ApiVersionProperties apiVersionProperties;

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new VersionedRequestMappingHandlerMapping(apiVersionProperties);
    }

    
}
