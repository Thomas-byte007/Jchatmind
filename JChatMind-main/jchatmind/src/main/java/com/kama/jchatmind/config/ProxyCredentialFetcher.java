package com.kama.jchatmind.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * ???????????? DB/SMTP ??,??????
 *
 * ????? CredentialStore(???????),
 * ???????????? jchatmind-proxy ????,
 * JChatMind ????????
 */
@Slf4j
@Component
public class ProxyCredentialFetcher {

    @Value("${credential-proxy.url:http://localhost:18080}")
    private String proxyUrl;

    @Value("${credential-proxy.auth-token:}")
    private String authToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private DbCredential dbCredential;
    private SmtpCredential smtpCredential;

    @PostConstruct
    public void fetchCredentials() {
        try {
            log.info("Fetching credentials from proxy at {}...", proxyUrl);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("X-Proxy-Token", authToken);
            }

            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            var dbResponse = restTemplate.exchange(
                    proxyUrl + "/internal/credentials/db", org.springframework.http.HttpMethod.GET, entity, DbCredential.class);
            dbCredential = dbResponse.getBody();

            var smtpResponse = restTemplate.exchange(
                    proxyUrl + "/internal/credentials/smtp", org.springframework.http.HttpMethod.GET, entity, SmtpCredential.class);
            smtpCredential = smtpResponse.getBody();

            if (dbCredential == null || smtpCredential == null) {
                throw new RuntimeException("Proxy returned null credentials");
            }

            log.info("Credentials fetched successfully from proxy");
        } catch (Exception e) {
            log.error("Failed to fetch credentials from proxy at {}: {}", proxyUrl, e.getMessage());
            throw new RuntimeException("Cannot start without proxy credentials", e);
        }
    }

    // ===== DB credentials (primary) =====
    public String getDbUrl() { return dbCredential.getUrl(); }
    public String getDbUsername() { return dbCredential.getUsername(); }
    public String getDbPassword() { return dbCredential.getPassword(); }

    // ===== DB credentials (restricted, for Agent tools) =====
    public String getRestrictedDbUsername() { return dbCredential.getRestrictedUsername(); }
    public String getRestrictedDbPassword() { return dbCredential.getRestrictedPassword(); }

    // ===== SMTP credentials =====
    public String getSmtpHost() { return smtpCredential.getHost(); }
    public int getSmtpPort() { return smtpCredential.getPort(); }
    public String getSmtpUsername() { return smtpCredential.getUsername(); }
    public String getSmtpPassword() { return smtpCredential.getPassword(); }

    @Data
    public static class DbCredential {
        private String url;
        private String username;
        private String password;
        private String restrictedUsername;
        private String restrictedPassword;
    }

    @Data
    public static class SmtpCredential {
        private String host;
        private int port;
        private String username;
        private String password;
    }
}
