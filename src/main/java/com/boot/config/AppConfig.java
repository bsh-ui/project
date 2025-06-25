package com.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Properties; // Properties 클래스 임포트

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ⭐ JavaMailSender 설정을 위한 속성 주입 (주석 해제) ⭐
    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean mailSmtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean mailSmtpStartTlsEnable;

    // mail.smtp.ssl.trust는 보통 필요 없지만, 있다면 추가합니다.
    // 만약 application.properties에 없다면 이 @Value 라인도 주석 처리해야 합니다.
    @Value("${spring.mail.properties.mail.smtp.ssl.trust:*}") // 기본값으로 *을 주어 MissingValueException 방지
    private String mailSmtpSslTrust;

    // ⭐ JavaMailSender Bean 정의 (주석 해제) ⭐
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailSmtpAuth);
        props.put("mail.smtp.starttls.enable", mailSmtpStartTlsEnable);
        props.put("mail.debug", "true"); // 메일 전송 디버깅을 위해 true로 설정할 수 있습니다.
        props.put("mail.smtp.starttls.required", "true"); // starttls.enable과 함께 필요한 경우가 많습니다.

        // mail.smtp.ssl.trust 속성이 application.properties에 있다면 추가
        if (mailSmtpSslTrust != null && !mailSmtpSslTrust.isEmpty()) {
            props.put("mail.smtp.ssl.trust", mailSmtpSslTrust);
        }

        return mailSender;
    }
}
