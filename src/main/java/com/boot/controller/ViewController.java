package com.boot.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Controller
@Slf4j
public class ViewController {

	private final ResourceLoader  resourceLoader;

    public ViewController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
	 @GetMapping("/custom_login")
	    public String customLoginPage(
	            @RequestParam(value = "error", required = false) String error,
	            @RequestParam(value = "message", required = false) String message, // ⭐ message 파라미터 추가
	            Model model) {
	        
	        if (error != null) {
	            // "error" 파라미터가 있으면 (로그인 실패 시)
	            // message 파라미터가 넘어왔다면 그것을 사용하고, 아니면 기본 메시지
	            model.addAttribute("loginError", message != null ? message : "로그인에 실패했습니다.");
	        }
	        return "custom_login";
	    }

    @GetMapping("/") // 애플리케이션 시작 시 기본 경로: http://localhost:8485/
    public String mainPage() {
        return "main"; // src/main/resources/templates/main.html 렌더링
    }

    @GetMapping("/main") // 명시적으로 /main 경로도 처리: http://localhost:8485/main
    public String redirectToMain() {
        return "main"; // src/main/resources/templates/main.html 렌더링
    }

    @GetMapping("/signup") // 자체 회원가입 페이지: http://localhost:8485/signup
    public String signUpForm() {
        return "signup"; // src/main/resources/templates/signup.html 렌더링
    }
    @GetMapping("/profile") // URL 경로: http://localhost:8485/profile
    public String profilePage() {
        return "profile"; // src/main/resources/templates/profile.html 렌더링
    }
    @GetMapping("/forgot_password")
    public String forgotPasswordPage() {
        return "forgot_password"; // src/main/resources/templates/forgot_password.html 렌더링
    }
    @GetMapping("/notice_admin") // 이 URL로 요청이 오면
    public String showNoticeAdminPage() {
        return "notice_admin"; // 'src/main/resources/templates/notice_admin.html' 파일을 렌더링
    }
    @GetMapping("/notice_list")
    public String noticeListPage() {
        return "notice_list";
    }
    @GetMapping("/notice_detail") // 이 URL로 요청이 들어오면
    public String noticeDetailPage() {
        return "notice_detail"; // src/main/resources/templates/notice_detail.html 템플릿을 찾아서 렌더링
    }
    @GetMapping("/music_upload")
    public String music_admin_upload() {
    	return "music_admin_upload";
    }
    @GetMapping("/music_detail")
    public String showMusicDetailPage(@RequestParam(value = "id", required = false) Long musicId) {
        log.info("음악 상세 페이지 요청: ID={}", musicId);
        return "music_detail"; 
    }
    @GetMapping("/my_playlists")
    public String myPlaylists() {
        return "my_playlists"; // my_playlists.html을 반환
    }
    @GetMapping("/index")
    public String index() {
        return "index"; // my_playlists.html을 반환
    }
    @GetMapping("/board")
    public ResponseEntity<String> showReactBoard() {
        try {
            // src/main/resources/static/index.html 파일을 직접 로드
            Resource resource = resourceLoader.getResource("classpath:/static/index.html");
            String htmlContent = resource.getContentAsString(StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);
        } catch (IOException e) {
            // 파일을 찾을 수 없거나 읽을 수 없는 경우
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("React app entry point not found.");
        }
    }
}
