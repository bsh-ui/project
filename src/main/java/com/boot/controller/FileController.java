package com.boot.controller;

import com.boot.exception.MyFileNotFoundException;
import com.boot.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files") // 이 컨트롤러의 기본 경로
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    // 앨범 커버 이미지를 제공하는 엔드포인트
    // 예: /api/files/cover-image/7e2d8075-166a-4a81-99a4-2f0fd6038dc1.png
    @GetMapping("/cover-image/{filename}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable("filename") String filename){
        log.info("앨범 커버 이미지 요청: {}", filename);
        try {
            // FileStorageService를 통해 'cover-image' 디렉토리에서 해당 파일을 로드
            // fileStorageService.loadFileAsResource는 절대 경로를 받도록 되어있으므로,
            // 파일명을 통해 절대 경로를 구성해야 합니다.
            // FileStorageService에 서브디렉토리와 파일명을 기반으로 절대 경로를 얻는 메서드가 필요합니다.
            String fullPath = fileStorageService.getFilePath("cover-image", filename);
            Resource resource = fileStorageService.loadFileAsResource(fullPath);

            // 파일의 실제 MIME 타입 결정
            String contentType = null;
            try {
                contentType = Files.probeContentType(resource.getFile().toPath());
            } catch (IOException ex) {
                log.warn("파일의 MIME 타입을 결정할 수 없습니다: {}", filename, ex);
            }

            // 기본 컨텐츠 타입 설정 (이미지가 아닐 경우)
            if (contentType == null) {
                contentType = "application/octet-stream"; // 일반 바이너리 스트림
            } else if (!contentType.startsWith("image/")) {
                // 이미지 파일이 아닐 경우 경고 (잘못된 파일이 업로드된 경우)
                log.warn("요청된 파일 '{}'은 이미지 파일이 아닙니다. Content-Type: {}", filename, contentType);
                // 그래도 일단 전송하지만, 브라우저가 렌더링하지 못할 수 있음
            }


            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MyFileNotFoundException ex) {
            log.error("앨범 커버 파일 없음: {}", filename, ex);
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (Exception ex) {
            log.error("앨범 커버 제공 중 오류 발생: {}", filename, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    // 다른 파일 유형 (예: 프로필 사진 등)을 위한 엔드포인트도 여기에 추가할 수 있습니다.
}