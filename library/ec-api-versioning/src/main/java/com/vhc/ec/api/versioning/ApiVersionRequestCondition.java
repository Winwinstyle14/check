package com.vhc.ec.api.versioning;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    private final String apiVersion;
    private final ApiVersionProperties apiVersionProperties;

    public ApiVersionRequestCondition(@NonNull String apiVersion, @NonNull ApiVersionProperties apiVersionProperties) {
        this.apiVersion = apiVersion.trim();
        this.apiVersionProperties = apiVersionProperties;
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        return new ApiVersionRequestCondition(other.getApiVersion(), other.getApiVersionProperties());
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        ApiVersionProperties.Type type = apiVersionProperties.getType();
        String version = null;
        switch (type) {
            case HEADER:
                version = request.getHeader(apiVersionProperties.getHeader());
                break;
            case PARAM:
                version = request.getParameter(apiVersionProperties.getParam());
                break;
        }
        boolean match = version != null && version.length() > 0 && version.trim().equals(apiVersion);
        if (match) {
            return this;
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        return other.getApiVersion().compareTo(getApiVersion());
    }

    @Override
    public String toString() {
        return "@ApiVersion(" + apiVersion + ")";
    }
}
