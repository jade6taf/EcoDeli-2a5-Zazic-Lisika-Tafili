package com.ecodeli.ecodeli_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${MAIL_HOST}")
    private String mailHost;

    @Value("${MAIL_PORT}")
    private int mailPort;

    @Value("${MAIL_USERNAME}")
    private String mailUsername;

    @Value("${MAIL_PASSWORD}")
    private String mailPassword;

    @Value("${MAIL_SMTP_AUTH}")
    private boolean smtpAuth;

    @Value("${MAIL_SMTP_STARTTLS}")
    private boolean smtpStartTls;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", smtpStartTls);
        props.put("mail.debug", "false");

        return mailSender;
    }
}
