package com.kama.jchatmind.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * CORS???? -- ????(????/??)????API
 * ???????:??????????(CORS??),?????????
 * localhost:* / 127.0.0.1:* ??????;???????????
 * allowCredentials(true):????Cookie???
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                    // ??????
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)               // ????Cookie
                .maxAge(3600);                        // ??????1??
    }
}