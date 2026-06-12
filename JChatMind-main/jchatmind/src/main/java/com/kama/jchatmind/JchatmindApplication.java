/*
 * Spring Boot???? -- ??Java?????
 * @SpringBootApplication:??????(@Configuration + @EnableAutoConfiguration + @ComponentScan)
 * SpringApplication.run():????Tomcat?????Bean?????HTTP??
 * ????8080,??? --server.port=9090 ??
 */
package com.kama.jchatmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,  // ?? DataSourceConfig ????????
        MailSenderAutoConfiguration.class   // ?? MailConfig ???? JavaMailSender
})
public class JchatmindApplication {

    public static void main(String[] args) {
        SpringApplication.run(JchatmindApplication.class, args);
    }
}