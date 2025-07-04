package com.boot.controller;

import com.boot.dto.MusicDTO;
import com.boot.dto.UserDTO;
// import com.boot.security.CustomUserDetails; // CustomUserDetails 임포트 제거
import com.boot.domain.User; // User 엔티티 임포트 추가 (com.boot.domain 패키지에 User가 있다고 가정)
import com.boot.service.MusicService;
import com.boot.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails는 필요하므로 유지
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest; // HttpServletRequest 임포트

import java.io.IOException;

@RestController
@RequestMapping("/api/music") // 일반적으로 REST API는 /api 접두사를 사용
@RequiredArgsConstructor
@Slf4j
public class MusicController {

    private final MusicService musicService;
    private final UserService userService; // UserService 주입

    // 1. 음악 업로드 (관리자 권한 필요)
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')") // ⭐ 주석 해제하여 관리자 권한 필수 적용 ⭐
    public ResponseEntity<?> uploadMusic( // ResponseEntity<MusicDTO> 대신 ResponseEntity<?>로 변경하여 에러 메시지 반환 유연하게
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam("lyricsContent") String lyricsContent,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user, // ⭐ UserDetails 대신 User로 변경 ⭐
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile // <-- 이 부분
    		) {
        log.info("음악 업로드 요청: 제목={}, 아티스트={}, 앨범={}, 파일명={}", title, artist, album, file.getOriginalFilename());

        if (user == null) { // userDetails 대신 user 사용
            log.warn("인증되지 않은 사용자의 음악 업로드 시도.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인된 사용자 정보가 없습니다.");
        }

        String username = user.getUsername(); // 로그인한 사용자의 username (User 엔티티에서 가져옴)

        // ⭐ UserService를 통해 UserDTO를 가져와 ID를 추출 ⭐
        // UserDTO는 세션에 저장된 정보이므로, Service를 통해 DB에서 한 번 더 조회하여 최신 상태의 ID를 가져옵니다.
        // 하지만 이미 @AuthenticationPrincipal User user로 User 엔티티를 직접 받았으므로,
        // 별도로 userService.findUserByUsernameOrEmail(username)을 호출하여 UserDTO로 변환할 필요 없이
        // user.getId()를 바로 사용해도 됩니다.
        // 현재 로직을 유지하려면 아래처럼 UserDTO로 변환하거나, 더 효율적으로 user.getId()를 직접 사용하세요.
        
        // Option 1: 현재 로직 유지 (단, user는 이미 User 엔티티이므로 toDTO()가 없다면 문제 발생)
        // UserDTO loggedInUser = userService.findUserByUsernameOrEmail(username); 
        // Long uploaderId = loggedInUser.getId();

        // Option 2: 더 효율적인 방법 (권장) - User 엔티티의 ID를 직접 사용
        Long uploaderId = user.getId(); // ⭐ 로그인한 사용자의 실제 ID ⭐

        log.info("업로더 사용자 ID: {}", uploaderId);
        
        try {
            MusicDTO uploadedMusic = musicService.uploadMusic(
            		title, 
            		artist,
            		album,
            		lyricsContent,
            		null,
            		null,
            		null,
            		null,
            		file,
            		uploaderId,
            		coverImageFile);
            		

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/music/stream/")
                    .path(uploadedMusic.getId().toString())
                    .toUriString();

            log.info("음악 업로드 성공: ID={}, 제목={}", uploadedMusic.getId(), uploadedMusic.getTitle());
            return ResponseEntity.ok(uploadedMusic);

        } catch (Exception e) {
            log.error("음악 업로드 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("음악 업로드 실패: " + e.getMessage());
        }
    }


    // 2. 모든 음악 조회 (페이지네이션)
    @GetMapping
    public ResponseEntity<Page<MusicDTO>> getAllMusic(
            @PageableDefault(size = 10, sort = "uploadDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<MusicDTO> musicPage = musicService.getAllMusic(pageable);
        return ResponseEntity.ok(musicPage);
    }

    // 3. 특정 음악 상세 조회
    @GetMapping("/{musicId}") // 예: /api/music/123
    public ResponseEntity<MusicDTO> getMusicDetail(@PathVariable("musicId") Long musicId) {
        log.info("음악 상세 정보 조회 요청: Music ID={}", musicId);
        try {
            MusicDTO musicDTO = musicService.getMusicById(musicId);
            return ResponseEntity.ok(musicDTO); // 200 OK
        } catch (IllegalArgumentException e) {
            log.error("음악 상세 정보 조회 실패 - 음악을 찾을 수 없음: Music ID={}, 에러: {}", musicId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        } catch (Exception e) {
            log.error("음악 상세 정보 조회 중 서버 오류 발생: Music ID={}", musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    // 4. 음악 파일 스트리밍 (음악 재생)
    @PreAuthorize("isAuthenticated()") // 로그인된 사용자만 접근 허용
    @GetMapping("/stream/{id}") // URL 경로에 포함된 {id}
    public ResponseEntity<Resource> streamMusic(@PathVariable("id") Long musicId,
                                                @AuthenticationPrincipal User user) { // ⭐ CustomUserDetails 대신 User로 변경 ⭐

        if (user == null) { // ⭐ userDetails 대신 user 사용 ⭐
            log.warn("비로그인 사용자의 음악 스트리밍 요청 차단: Music ID = {}", musicId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "스트리밍을 위해서는 로그인이 필요합니다.");
        }

        log.info("음악 스트리밍 요청: Music ID = {}", musicId);
        try {
            Resource musicResource = musicService.loadMusicFileAsResource(musicId); // musicId 사용

            String contentType = "audio/mpeg"; // 또는 musicService에서 파일 타입에 따라 동적으로 결정

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + musicResource.getFilename() + "\"")
                    .body(musicResource);
        } catch (Exception e) {
            log.error("음악 스트리밍 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 또는 다른 오류 응답
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @musicService.isMusicUploader(#id, authentication.principal.id)") // ⭐ 본인 업로드 또는 관리자만 삭제 가능 ⭐
    public ResponseEntity<Void> deleteMusic(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user // ⭐ User 객체 주입 ⭐
    ) {
        // @PreAuthorize 덕분에 user가 null일 가능성은 낮지만, 방어적으로 한 번 더 체크
        if (user == null) {
            log.warn("인증되지 않은 사용자의 음악 삭제 시도 (ID: {})", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // @PreAuthorize에서 이미 권한을 검증하므로, service 메소드에서는 직접적인 사용자 ID 검증 로직이 필요 없을 수 있음.
            // 하지만 서비스 계층에서 한 번 더 검증하는 것이 견고성을 높일 수 있습니다.
            musicService.deleteMusic(id);
            log.info("음악 삭제 성공: ID={}", id);
            return ResponseEntity.noContent().build(); // 204 No Content (삭제 성공)
        } catch (IllegalArgumentException e) {
            log.error("음악 삭제 실패 - 음악을 찾을 수 없음: Music ID={}, 에러: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            log.error("음악 삭제 실패 - 권한 없음: Music ID={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("음악 삭제 중 서버 오류 발생: Music ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ⭐⭐⭐ 음악 수정 API 추가 ⭐⭐⭐
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @musicService.isMusicUploader(#id, authentication.principal.id)") // ⭐ 본인 업로드 또는 관리자만 수정 가능 ⭐
    public ResponseEntity<MusicDTO> updateMusic(
            @PathVariable("id") Long id,
            @RequestBody MusicDTO musicDto,
            @AuthenticationPrincipal User user // ⭐ User 객체 주입 ⭐
    ) {
        if (user == null) {
            log.warn("인증되지 않은 사용자의 음악 수정 시도 (ID: {})", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            MusicDTO updatedMusic = musicService.updateMusic(id, musicDto, null, null);
            log.info("음악 수정 성공: ID={}, 제목={}", updatedMusic.getId(), updatedMusic.getTitle());
            return ResponseEntity.ok(updatedMusic); // 200 OK (수정된 객체 반환)
        } catch (IllegalArgumentException e) {
            log.error("음악 수정 실패 - 음악을 찾을 수 없음: Music ID={}, 에러: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (SecurityException e) {
            log.error("음악 수정 실패 - 권한 없음: Music ID={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("음악 수정 중 서버 오류 발생: Music ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/search") // 예: /api/music/search?keyword=제목&page=0&size=10
    public ResponseEntity<Page<MusicDTO>> searchMusic(
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 10, sort = "uploadDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("음악 검색 요청 수신: 키워드='{}', 페이지={}", keyword, pageable.getPageNumber());
        try {
            Page<MusicDTO> searchResults = musicService.searchMusic(keyword, pageable);
            return ResponseEntity.ok(searchResults); // 200 OK
        } catch (Exception e) {
            log.error("음악 검색 중 오류 발생: 키워드={}, 에러: {}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
    // TODO: 재생 횟수 증가 API (@PostMapping("/play/{id}"))
}

