package com.kama.jchatmind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * ?????? -- ???? JavaMailSender Bean
 *
 * ?? Spring Boot ? spring.mail.* ????,??? ProxyCredentialFetcher ??,
 * JChatMind ??????????????????
 */
@Configuration
public class MailConfig {

    private final ProxyCredentialFetcher credentials;

    public MailConfig(ProxyCredentialFetcher credentials) {
        this.credentials = credentials;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(credentials.getSmtpHost());
        sender.setPort(credentials.getSmtpPort());
        sender.setUsername(credentials.getSmtpUsername());
        sender.setPassword(credentials.getSmtpPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        return sender;
    }

    /**
     * ??????? Bean -- ? EmailServiceImpl ????
     */
    @Bean(name = "mailFromAddress")
    public String mailFromAddress() {
        return credentials.getSmtpUsername();
    }
}
