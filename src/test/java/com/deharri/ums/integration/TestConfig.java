package com.deharri.ums.integration;

import com.amazonaws.services.s3.AmazonS3;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        return Mockito.mock(AmazonS3.class);
    }

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    @Primary
    public SimpleMailMessage simpleMailMessage() {
        return new SimpleMailMessage();
    }
}
