package com.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Autowired
    public WebConfig(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 모든 업로드 파일을 서빙하기 위한 리소스 핸들러 추가
        // URL 패턴: /uploads/**
        // 실제 파일 시스템 경로: file.upload-dir (예: ./uploads)
        // file:/// 접두사는 로컬 파일 시스템 경로를 나타냅니다.

        Path uploadDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize(); // 변경: getProfileImageLocation() -> getUploadDir()
        String uploadPath = "file:///" + uploadDir.toString().replace("\\", "/"); // OS별 경로 구분자 통일

        registry.addResourceHandler("/uploads/**") // URL 패턴 변경: /profile-images/** -> /uploads/**
                .addResourceLocations(uploadPath + "/");
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api로 시작하는 모든 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:8485", "http://127.0.0.1:8485","http://localhost:3000", "http://127.0.0.1:3000") // React 개발 서버 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 자격 증명 (쿠키, HTTP 인증) 허용
                .maxAge(3600); // Pre-flight 요청 캐싱 시간 (초)
    }
}