/*
 * ?????? -- ??Spring Mail + QQ??SMTP??????
 * @Async:?????????,???????????(???Agent????)
 * SimpleMailMessage:Spring????????,???HTML/??
 * ?????? MailConfig.mailFromAddress Bean ??,???? ProxyCredentialFetcher
 */
package com.kama.jchatmind.service.impl;

import com.kama.jchatmind.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    // Spring Mail?????,?????JavaMail?????
    private final JavaMailSender mailSender;

    // ?????,? MailConfig ? Bean ??(???? ProxyCredentialFetcher)
    private final String from;
    
    public EmailServiceImpl(JavaMailSender mailSender,
                            @Qualifier("mailFromAddress") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    @Async  // ????:???????,???????
    public void sendEmailAsync(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom(from);
            mailSender.send(message);
            log.info("????????,???: {}, ??: {}", to, subject);
        } catch (Exception e) {
            log.error("????????,???: {}, ??: {}, ??: {}", to, subject, e.getMessage(), e);
        }
    }
}