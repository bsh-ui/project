package com.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.javamail.JavaMailSender; // ⭐ 이 임포트도 추가
import org.springframework.mail.javamail.JavaMailSenderImpl; // ⭐ 이 임포트도 추가
import org.springframework.beans.factory.annotation.Value; // ⭐ 이 임포트도 추가

import java.util.Properties; // ⭐ 이 임포트도 추가

@Configuration // ⭐ 이 어노테이션이 있어야 합니다.
public class AppConfig {

    @Bean // ⭐ 이 어노테이션이 있어야 합니다.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
//
//    // ⭐ 여기에 JavaMailSender 빈 정의도 있어야 합니다.
//    @Value("${spring.mail.host}")
//    private String mailHost;
//    @Value("${spring.mail.port}")
//    private int mailPort;
//    @Value("${spring.mail.username}")
//    private String mailUsername;
//    @Value("${spring.mail.password}")
//    private String mailPassword;
//    @Value("${spring.mail.properties.mail.smtp.auth}")
//    private boolean mailSmtpAuth;
//    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
//    private boolean mailSmtpStarttlsEnable;
//    @Value("${spring.mail.properties.mail.smtp.ssl.trust}")
//    private String mailSmtpSslTrust;
//
//    @Bean
//    public JavaMailSender javaMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(mailHost);
//        mailSender.setPort(mailPort);
//        mailSender.setUsername(mailUsername);
//        mailSender.setPassword(mailPassword);
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", mailSmtpAuth);
//        props.put("mail.smtp.starttls.enable", mailSmtpStarttlsEnable);
//        props.put("mail.smtp.ssl.trust", mailSmtpSslTrust); 
//
//        return mailSender;
//    }
//}