package com.boot.controller;

import com.boot.dto.UserDTO;
import com.boot.exception.UserNotFoundException;
import com.boot.service.EmailService; // EmailService 임포트 추가
import com.boot.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException; // MailException 임포트 추가

import java.util.HashMap;
import java.util.Map;

@Controller // 뷰를 반환하므로 @Controller 사용 (API 요청에는 @ResponseBody를 추가)
@RequiredArgsConstructor
@Slf4j
public class MypageController {

    private final UserService userService;
    private final EmailService emailService; // EmailService 주입

    // 마이페이지 뷰를 렌더링
    @GetMapping("/mypage")
    public String myPage(Model model, HttpSession session) {
        log.info("마이페이지 접근 시도.");
        UserDTO userFromSession = (UserDTO) session.getAttribute("user");

        if (userFromSession != null) {
            log.info("세션에서 사용자 정보 '{}' 로드. 마이페이지 렌더링.", userFromSession.getUsername());
            model.addAttribute("user", userFromSession);
            return "mypage"; // mypage.html 렌더링
        } else {
            // 세션에 userDTO가 없을 경우, SecurityContextHolder에서 다시 가져오기 시도
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername(); // 로그인 식별자 (아이디 또는 이메일)
                log.info("SecurityContextHolder에서 사용자 '{}' 정보 로드 시도.", username);
                
                UserDTO userFromDB = userService.getUserByUsername(username); // 또는 getUserByEmail
                
                if (userFromDB != null) {
                    log.info("DB에서 사용자 '{}' 정보 로드 성공. 마이페이지 렌더링.", username);
                    session.setAttribute("user", userFromDB); // 세션에도 저장
                    model.addAttribute("user", userFromDB);
                    return "mypage";
                }
            }
        }
        
        // 어떤 방법으로도 사용자 정보를 가져올 수 없으면 로그인 페이지로 리다이렉트
        log.warn("인증되지 않거나 사용자 정보를 찾을 수 없어 마이페이지 접근 실패. 로그인 페이지로 리다이렉트합니다.");
        return "redirect:/custom_login";
    }


    // 사용자 정보 수정 API
    @PutMapping("/api/mypage/update")
    @ResponseBody // JSON 응답을 위해 필요
    public ResponseEntity<?> updateUserInfo(@RequestBody UserDTO userDTO, HttpSession session) {
        log.info("사용자 정보 수정 요청: {}", userDTO.getUsername() != null ? userDTO.getUsername() : "Unknown User");
        
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null) {
            log.warn("사용자 정보 수정 실패: 세션에 인증된 사용자 정보가 없음.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        
        Long userId = currentUser.getId(); // 세션의 UserDTO에서 ID 가져옴 (로그인된 사용자 본인의 정보만 수정 가능)

        try {
            // UserDTO는 클라이언트에서 보낸 수정할 정보만 담고 있음
            UserDTO updatedUser = userService.updateUserInfo(userId, userDTO);
            // 세션 정보도 업데이트 (필수)
            session.setAttribute("user", updatedUser);
            log.info("사용자 정보 수정 성공: ID {}", userId);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.warn("사용자 정보 수정 실패: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("사용자 정보 수정 중 오류 발생", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "사용자 정보 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 비밀번호 변경 API
    @PatchMapping("/api/mypage/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwords, HttpSession session) {
        log.info("비밀번호 변경 요청");
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null) {
            log.warn("비밀번호 변경 실패: 세션에 인증된 사용자 정보가 없음.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        Long userId = currentUser.getId();

        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");

        if (oldPassword == null || newPassword == null || newPassword.isEmpty()) {
            log.warn("비밀번호 변경 실패: 기존 비밀번호 또는 새 비밀번호 누락.");
            return ResponseEntity.badRequest().body("기존 비밀번호와 새 비밀번호를 모두 입력해야 합니다.");
        }

        try {
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                log.info("비밀번호 변경 성공: 사용자 ID {}", userId);
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                log.warn("비밀번호 변경 실패: 기존 비밀번호 불일치 (사용자 ID {})", userId);
                return ResponseEntity.badRequest().body("기존 비밀번호가 일치하지 않습니다.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("비밀번호 변경 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }

    // 계정 삭제 API
    @DeleteMapping("/api/mypage/delete")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(HttpSession session) {
        log.info("계정 삭제 요청");
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null) {
            log.warn("계정 삭제 실패: 세션에 인증된 사용자 정보가 없음.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        Long userId = currentUser.getId();

        try {
            userService.deleteUser(userId);
            // 계정 삭제 후 세션 무효화 및 로그아웃 처리
            session.invalidate(); // 현재 세션 무효화
            log.info("사용자 계정 삭제 성공: ID {}", userId);
            return ResponseEntity.ok("계정이 성공적으로 삭제되었습니다. 로그인 페이지로 돌아갑니다.");
        } catch (Exception e) {
            log.error("계정 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계정 삭제 중 오류가 발생했습니다.");
        }
    }

    // 비밀번호 찾기 이메일 입력 페이지를 보여주는 메서드
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        log.info("비밀번호 찾기 페이지 접근");
        return "forgot_password"; // forgot_password.html 렌더링
    }

    // 비밀번호 재설정 이메일 발송 API
    @PostMapping("/api/forgot-password/send-email")
    @ResponseBody
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            log.warn("비밀번호 재설정 이메일 요청 실패: 이메일 주소 누락.");
            return ResponseEntity.badRequest().body("이메일 주소를 입력해주세요.");
        }

        log.info("비밀번호 재설정 이메일 요청: {}", email);
        try {
            // 1. UserService에서 토큰 생성 및 DB 저장
            String resetToken = userService.createPasswordResetTokenForUser(email);

            // 2. EmailService를 통해 이메일 발송
            emailService.sendPasswordResetEmail(email, resetToken); // EmailService 호출

            log.info("비밀번호 재설정 링크 이메일 발송 요청 및 메일 전송 성공: {}", email);
            return ResponseEntity.ok("비밀번호 재설정 링크가 이메일로 전송되었습니다. 이메일을 확인해주세요.");
        } catch (UserNotFoundException e) { // 사용자를 찾지 못했을 때
            log.warn("비밀번호 재설정 이메일 발송 실패: 사용자 없음. {}", email);
            // 보안을 위해 사용자에게는 정확한 정보를 제공하지 않고, 성공한 것처럼 보이게 합니다.
            // 클라이언트에게는 성공 메시지를 반환하여 이메일 존재 여부를 추측하기 어렵게 합니다.
            return ResponseEntity.ok("비밀번호 재설정 링크가 이메일로 전송되었습니다. 이메일을 확인해주세요.");
        } catch (MailException e) { // 메일 전송 중 오류 (MailAuthenticationException 등)
            log.error("비밀번호 재설정 이메일 발송 중 MailException 발생: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) { // 그 외 모든 예상치 못한 오류
            log.error("비밀번호 재설정 이메일 발송 중 예상치 못한 오류 발생: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다. 관리자에게 문의해주세요.");
        }
    }
    
    // 아이디 찾기 이메일 발송 API (현재 보류 중인 기능)
    @PostMapping("/api/find-id/send-email")
    @ResponseBody
    public ResponseEntity<?> sendIdByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            log.warn("아이디 찾기 이메일 요청 실패: 이메일 주소 누락.");
            return ResponseEntity.badRequest().body("이메일 주소를 입력해주세요.");
        }

        log.info("아이디 찾기 이메일 요청: {}", email);
        try {
            UserDTO username = userService.findUserByUsernameOrEmail(email); // UserSerivce에 구현 필요

            // 이메일 서비스 호출 (EmailService에 sendIdInfoEmail 메서드 추가 필요)
            // emailService.sendIdInfoEmail(email, username);
            
            log.info("아이디 찾기 정보 이메일 발송 요청 성공: {}", email);
            // 보안을 위해 실제 아이디 노출 대신 "이메일로 발송되었다"는 메시지 전달
            return ResponseEntity.ok("회원님의 아이디 정보가 이메일로 전송되었습니다. 이메일을 확인해주세요.");
        } catch (UserNotFoundException e) {
            log.warn("아이디 찾기 이메일 발송 실패: 사용자 없음. {}", email);
            // 보안을 위해 사용자에게는 정확한 정보를 제공하지 않고, 성공한 것처럼 보이게 합니다.
            return ResponseEntity.ok("회원님의 아이디 정보가 이메일로 전송되었습니다. 이메일을 확인해주세요.");
        } catch (MailException e) {
            log.error("아이디 찾기 이메일 발송 중 MailException 발생: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) {
            log.error("아이디 찾기 이메일 발송 중 예상치 못한 오류 발생: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다. 관리자에게 문의해주세요.");
        }
    }
    
    // TODO: 이메일 인증 (회원가입/계정 활성화) 관련 엔드포인트도 유사하게 구현
    //      예시: @PostMapping("/api/auth/verify-email/send-code")
    //            public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> request) { ... }
    //
    // TODO: 비밀번호 재설정 완료 (링크 클릭 후 새 비밀번호 입력) 엔드포인트
    //      예시: @PostMapping("/api/forgot-password/reset-password")
    //            public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) { ... }
    
    // TODO: 이메일 인증 코드 확인 엔드포인트
    //      예시: @PostMapping("/api/auth/verify-email/confirm-code")
    //            public ResponseEntity<?> confirmVerificationCode(@RequestBody VerificationRequest request) { ... }

}