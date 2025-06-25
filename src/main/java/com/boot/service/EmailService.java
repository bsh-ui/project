package com.boot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper; // MimeMessageHelper 임포트
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException; // 예외 처리 임포트
import jakarta.mail.internet.MimeMessage; // MimeMessage 임포트

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    // 기존 sendEmail 메서드 (텍스트 전용)는 유지하거나 삭제
    // public void sendEmail(String to, String subject, String text) throws MailException {
    //     SimpleMailMessage message = new SimpleMailMessage();
    //     message.setTo(to);
    //     message.setFrom("3455119@naver.com");
    //     message.setSubject(subject);
    //     message.setText(text);
    //     javaMailSender.send(message);
    //     System.out.println("메일 전송 성공! (받는 사람: " + to + ", 제목: " + subject + ")");
    // }

    // HTML 내용을 포함한 이메일을 보낼 수 있는 새로운 sendHtmlEmail 메서드 추가
    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException { // <-- 오버로딩 또는 메서드명 변경
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // true는 multipart 메시지임을 의미 (HTML 콘텐츠를 사용하기 위해)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("3455119@naver.com"); // 발신자 이메일
            helper.setTo(to); // 수신자 이메일
            helper.setSubject(subject); // 제목
            helper.setText(htmlContent, true); // 두 번째 인자를 true로 설정하여 HTML 콘텐츠임을 명시

            javaMailSender.send(mimeMessage);
            log.info("HTML 메일 전송 성공! (받는 사람: {}, 제목: {})", to, subject);
        } catch (jakarta.mail.MessagingException e) { // jakarta.mail.MessagingException
            log.error("HTML 메일 전송 실패! (받는 사람: {}, 제목: {})", to, subject, e);
            throw new MessagingException("HTML 메일 전송 중 오류 발생", e); // 다시 던져서 상위에서 처리
        } catch (MailException e) { // Spring Mail의 예외 처리
            log.error("Spring Mail 전송 실패! (받는 사람: {}, 제목: {})", to, subject, e);
            throw e; // Spring Mail 예외는 그대로 던짐
        }
    }


    // 비밀번호 재설정 이메일 전송 메서드 (EmailService 내부에서 직접 HTML을 구성하도록 변경)
    public void sendPasswordResetEmail(String toEmail, String token) throws MessagingException { // throws MailException 제거, MessagingException으로 통합
        String subject = "[ListenIt] 비밀번호 재설정 안내";
        // Front-end에서 reset-password 페이지가 있다면 해당 URL 사용
        String resetUrl = "http://localhost:8485/reset-password?token=" + token; // UserService와 동일하게 설정

        String htmlContent = "<html><body>"
                           + "<h3>비밀번호 재설정 안내</h3>"
                           + "<p>비밀번호를 재설정하려면 다음 링크를 클릭해주세요:</p>"
                           + "<p><a href=\"" + resetUrl + "\">비밀번호 재설정하기</a></p>"
                           + "<p>이 링크는 한 시간 동안 유효합니다.</p>"
                           + "<p>만약 본인이 요청하지 않았다면, 이 이메일을 무시해주세요.</p>"
                           + "</body></html>";
        
        sendEmail(toEmail, subject, htmlContent); // HTML 전송 메서드 호출
    }

    // 이메일 인증 코드 전송 메서드 예시
    public void sendVerificationEmail(String toEmail, String authCode) throws MessagingException { // throws MailException 제거
        String subject = "[ListenIt] 이메일 인증 코드";
        String htmlContent = "<html><body>"
                           + "<h3>이메일 인증 코드 안내</h3>"
                           + "<p>안녕하세요! ListenIt 입니다.</p>"
                           + "<p>귀하의 이메일 인증 코드는 다음과 같습니다: <strong>" + authCode + "</strong></p>"
                           + "<p>인증 절차를 완료하려면 이 코드를 입력해주세요. 이 코드는 일정 시간 후에 만료됩니다.</p>"
                           + "<p>감사합니다.</p>"
                           + "</body></html>";
        sendEmail(toEmail, subject, htmlContent); // HTML 전송 메서드 호출
    }
}