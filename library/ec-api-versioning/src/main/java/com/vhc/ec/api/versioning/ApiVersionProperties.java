package com.vhc.ec.api.versioning;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * Api version configuration
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "vhc.ec.api.version")
public class ApiVersionProperties implements Serializable {

    private Type type = Type.URI;

    private String uriPrefix;

    private UriLocation uriLocation = UriLocation.END;

    private String header = "X-API-VERSION";

    private String param = "api_version";

    public enum Type {
        URI,
        HEADER,
        PARAM
    }

    public enum UriLocation {
        BEGIN, END
    }
}
