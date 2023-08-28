package com.vhc.ec.api.versioning;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@AllArgsConstructor
public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * Multi-version configuration properties
     */
    ApiVersionProperties apiVersionProperties;

    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return createRequestCondition(handlerType);
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return createRequestCondition(method);
    }

    private RequestCondition<ApiVersionRequestCondition> createRequestCondition(AnnotatedElement target) {
        if (apiVersionProperties.getType() == ApiVersionProperties.Type.URI) {
            return null;
        }
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(target, ApiVersion.class);
        if (apiVersion == null) {
            return null;
        }
        String version = apiVersion.value().trim();
        InnerUtils.checkVersionNumber(version, target);
        return new ApiVersionRequestCondition(version, apiVersionProperties);
    }

    //--------------------- Dynamic registration URI -----------------------//
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = this.createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = this.createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }

            // Specify URL prefix
            if (apiVersionProperties.getType() == ApiVersionProperties.Type.URI) {
                ApiVersion apiVersion = AnnotationUtils.getAnnotation(method, ApiVersion.class);
                if (apiVersion == null) {
                    apiVersion = AnnotationUtils.getAnnotation(handlerType, ApiVersion.class);
                }
                if (apiVersion != null) {
                    String version = apiVersion.value().trim();
                    InnerUtils.checkVersionNumber(version, method);

                    String prefix = "/v" + version;
                    if (apiVersionProperties.getUriLocation() == ApiVersionProperties.UriLocation.END) {
                        info = info.combine(RequestMappingInfo.paths(prefix).build());
                    } else {
                        if (StringUtils.hasText(apiVersionProperties.getUriPrefix())) {
                            prefix = apiVersionProperties.getUriPrefix().trim() + prefix;
                        }
                        info = RequestMappingInfo.paths(prefix).build().combine(info);
                    }
                }
            }
        }

        return info;
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = element instanceof Class ? this.getCustomTypeCondition((Class) element) : this.getCustomMethodCondition((Method) element);
        return requestMapping != null ? this.createRequestMappingInfo(requestMapping, condition) : null;
    }
}
