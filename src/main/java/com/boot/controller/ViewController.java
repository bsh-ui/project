package com.boot.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Model 임포트 유지 (customLoginPage 때문)
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;


@Controller
@Slf4j
public class ViewController {

	private final ResourceLoader resourceLoader;

    public ViewController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // ⭐ React 앱에서 /login 경로를 처리하도록 위임하므로, 이 Thymeleaf 로그인 페이지는 필요 없을 수 있습니다.
    // ⭐ 하지만 Spring Security의 loginPage() 설정 때문에 필요할 수도 있습니다. 일단 유지합니다.
    @GetMapping("/custom_login")
    public String customLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "message", required = false) String message,
            Model model) {
        if (error != null) {
            model.addAttribute("loginError", message != null ? message : "로그인에 실패했습니다.");
        }
        return "custom_login"; // src/main/resources/templates/custom_login.html 렌더링
    }

    // ⭐⭐⭐ 이전에 충돌을 일으켰던 주석 처리된 @GetMapping("/") 제거됨. ⭐⭐⭐
    // SpaRoutingController가 "/" 경로를 담당합니다.

    // ⭐⭐ React 앱으로 모두 전환했다면, 아래의 모든 @GetMapping 메서드들은 사실상 불필요합니다. ⭐⭐
    // ⭐⭐ React Router가 클라이언트 측 라우팅을 담당하기 때문입니다. ⭐⭐
    // ⭐⭐ 이들을 모두 제거하고 싶다면, 주석 처리하거나 삭제해도 됩니다. ⭐⭐
    // ⭐⭐ 단, Spring Security 설정에서 이 경로들이 permitAll() 되어 있다면 계속 유지해도 무방합니다. ⭐⭐

    // 만약 `/main`이 React 앱 내부의 라우트라면, 이 컨트롤러는 불필요합니다.
    // SpaRoutingController가 /main을 index.html로 포워딩하고, React Router가 처리합니다.
    // @GetMapping("/main")
    // public String redirectToMain() {
    //     return "main"; // src/main/resources/templates/main.html 렌더링
    // }

    // React 앱에서 /signup 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/signup")
    // public String signUpForm() {
    //     return "signup"; // src/main/resources/templates/signup.html 렌더링
    // }

    // React 앱에서 /profile 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/profile")
    // public String profilePage() {
    //     return "profile"; // src/main/resources/templates/profile.html 렌더링
    // }

    // React 앱에서 /forgot-password 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/forgot_password")
    // public String forgotPasswordPage() {
    //     return "forgot_password"; // src/main/resources/templates/forgot_password.html 렌더링
    // }

    // React 앱에서 /admin/notice 또는 /notice_admin 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/notice_admin")
    // public String showNoticeAdminPage() {
    //     return "notice_admin";
    // }

    // React 앱에서 /notice-list 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/notice_list")
    // public String noticeListPage() {
    //     return "notice_list";
    // }

    // React 앱에서 /notice-detail/{id} 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/notice_detail")
    // public String noticeDetailPage() {
    //     return "notice_detail";
    // }

    // React 앱에서 /admin/music 또는 /music_upload 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/music_upload")
    // public String music_admin_upload() {
    // 	return "music_admin_upload";
    // }

    // React 앱에서 /music/{id} 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/music_detail")
    // public String showMusicDetailPage(@RequestParam(value = "id", required = false) Long musicId) {
    //     log.info("음악 상세 페이지 요청: ID={}", musicId);
    //     return "music_detail";
    // }

    // React 앱에서 /my-playlists 경로를 처리한다면 이 컨트롤러는 불필요합니다.
    // @GetMapping("/my_playlists")
    // public String myPlaylists() {
    //     return "my_playlists";
    // }

    // React 앱의 index.html을 직접 반환하는 SpaRoutingController의 역할과 중복됩니다.
    // @GetMapping("/index")
    // public String index() {
    //     return "index";
    // }

    // 이 메서드는 React 앱의 index.html을 직접 읽어서 반환합니다.
    // SpaRoutingController가 모든 SPA 경로를 처리하므로,
    // /board 같은 특정 경로로 직접 React 앱을 서빙할 필요는 없습니다.
    // 이 메서드는 제거하거나, SpaRoutingController로 통합하는 것을 권장합니다.
    // @GetMapping("/board")
    // public ResponseEntity<String> showReactBoard() {
    //     try {
    //         Resource resource = resourceLoader.getResource("classpath:/static/index.html");
    //         String htmlContent = resource.getContentAsString(StandardCharsets.UTF_8);
    //         return ResponseEntity.ok()
    //                 .contentType(MediaType.TEXT_HTML)
    //                 .body(htmlContent);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("React app entry point not found.");
    //     }
    // }
}
