package com.kama.jchatmind.service;

/**
 * ??????
 */
public interface EmailService {
    /**
     * ??????
     *
     * @param to      ???????
     * @param subject ????
     * @param content ????
     */
    void sendEmailAsync(String to, String subject, String content);
}
