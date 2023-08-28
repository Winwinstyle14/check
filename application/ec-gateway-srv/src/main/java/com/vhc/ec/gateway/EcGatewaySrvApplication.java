package com.vhc.ec.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class EcGatewaySrvApplication {

    private static final String ALLOWED_HEADERS = "x-requested-with, authorization, Content-Type, Content-Length, Authorization, credential, X-XSRF-TOKEN";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS, PATCH";
    private static final String ALLOWED_ORIGIN = "*";
    private static final String MAX_AGE = "7200"; //2 hours (2 * 60 * 60)

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            if (CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                headers.add("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
                headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
                headers.add("Access-Control-Max-Age", MAX_AGE); //OPTION how long the results of a preflight request (that is the information contained in the Access-Control-Allow-Methods and Access-Control-Allow-Headers headers) can be cached.
                headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(ctx);
        };
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path("/api/v1/groups/**", "/api/v1/auth/**", "/api/v1/token")
                        .filters(f -> f.setResponseHeader(
                                        "Access-Control-Allow-Origin", ALLOWED_ORIGIN
                                )
                        )
                        .uri("lb://ec-auth-srv"))
                .route(p -> p.path("/api/v1/admin/**")
                        .filters(f -> f.setResponseHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                        .uri("lb://ec-admin-srv"))
                .route(p -> p
                        .path("/api/v1/organizations/**",
                                "/api/v1/customers/**"
                        )
                        .filters(f -> f.setResponseHeader(
                                        "Access-Control-Allow-Origin", ALLOWED_ORIGIN
                                )
                        )
                        .uri("lb://ec-customer-srv"))
                .route(p -> p
                        .path(
                        		"/api/v1/upload/**",
                        		"/api/v1/tmp/**",
                                "/api/v1/tp/files/**"
                        )
                        .filters(f -> f.setResponseHeader(
                                        "Access-Control-Allow-Origin", ALLOWED_ORIGIN
                                )
                        )
                        .uri("lb://ec-file-srv"))
                .route(p -> p
                        .path(
                                "/api/v1/contract-types/**",
                                "/api/v1/contracts/**",
                                "/api/v1/participants/*",
                                "/api/v1/documents/*",
                                "/api/v1/fields/**",
                                "/api/v1/processes/**",
                                "/api/v1/batch/**",
                                "/api/v1/dashboard/**",
                                "/api/v1/sign/**",
                                "/api/v1/shares/**",
                                "/api/v1/recipients/**",
                                "/api/v1/ceca/**",
                                "/api/v1/tp/contracts/**",
                                "/api/v1/handle/**"
                        )
                        .filters(f -> f.setResponseHeader(
                                        "Access-Control-Allow-Origin", ALLOWED_ORIGIN
                                )
                        )
                        .uri("lb://ec-contract-srv"))
                .route(p -> p
                        .path("/api/v1/notification/**")
                        .filters(f -> f.setResponseHeader(
                                        "Access-Control-Allow-Origin", ALLOWED_ORIGIN
                                )
                        )
                        .uri("lb://ec-notification-srv"))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(EcGatewaySrvApplication.class, args);
    }

}
