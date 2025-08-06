package com.deharri.ums.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EMailConfig {

    @Value("${spring.mail.port}")
    private int MAIL_PORT;

    @Value("${spring.mail.host}")
    private  String MAIL_HOST;

    @Value("${spring.mail.username}")
    private String MAIL_USERNAME;

    @Value("${spring.mail.password}")
    private String MAIL_PASSWORD;

    @Bean
    JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setPort(MAIL_PORT);
        mailSender.setHost(MAIL_HOST);
        mailSender.setUsername(MAIL_USERNAME);
        mailSender.setPassword(MAIL_PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // ✅ Enable STARTTLS
        props.put("mail.smtp.starttls.required", "true"); // ✅ Enforce STARTTLS
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    SimpleMailMessage mailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(MAIL_USERNAME);
        return message;
    }

}
