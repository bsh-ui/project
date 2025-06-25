// src/main/java/com/boot/controller/NoticeController.java
package com.boot.controller;

import com.boot.domain.NoticeType; // ⭐ NoticeType 임포트 추가
import com.boot.dto.NoticeCreateRequestDto; // ⭐ NoticeCreateRequestDto 임포트 추가
import com.boot.dto.NoticeDto; // ⭐ NoticeDto 임포트 추가
import com.boot.service.NoticeService;
// import com.boot.util.SecurityUtil; // ⭐ SecurityUtil 사용 방식 변경되므로 임포트 주석 또는 삭제 (아래에서 UserDetails 사용)

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ⭐ Pageable 임포트 (PageRequest 대신 PageableDefault 사용 시)
import org.springframework.data.domain.Sort; // ⭐ Sort 임포트
import org.springframework.data.web.PageableDefault; // ⭐ PageableDefault 임포트
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ⭐ AuthenticationPrincipal 임포트
import org.springframework.security.core.userdetails.UserDetails; // ⭐ UserDetails 임포트
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException; // ⭐ EntityNotFoundException 임포트 (NoticeService와 일관되게)

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 1. 게시글 목록 조회 (모든 사용자 접근 가능) - 페이지네이션 포함
    // GET /api/notices?page=0&size=10&sort=createdAt,desc&type=NOTICE
    @GetMapping
    public ResponseEntity<Page<NoticeDto>> getNotices( // ⭐ 반환 타입 Notice -> NoticeDto
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, // ⭐ PageableDefault 사용
            @RequestParam(name = "type", required = false) NoticeType type // ⭐ String -> NoticeType 변경
    ) {
        Page<NoticeDto> noticePage = noticeService.getNoticeList(type, pageable); // ⭐ NoticeService 메서드 변경에 맞춤
        return ResponseEntity.ok(noticePage);
    }

    // 2. 특정 게시글 상세 조회 (모든 사용자 접근 가능)
    // GET /api/notices/{id}
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDto> getNoticeDetail(@PathVariable(name = "id") Long id){ // ⭐ 반환 타입 Notice -> NoticeDto
        try {
            NoticeDto notice = noticeService.getNoticeById(id); // ⭐ NoticeService 메서드 변경에 맞춤
            return ResponseEntity.ok(notice);
        } catch (EntityNotFoundException e) { // ⭐ IllegalArgumentException -> EntityNotFoundException
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 Not Found
            // 또는 return ResponseEntity.notFound().build();
        }
    }

    // 3. 새 게시글 생성 (ROLE_ADMIN 권한 필요)
    // POST /api/notices
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticeDto> createNotice(@RequestBody NoticeCreateRequestDto requestDto, // ⭐ Map -> NoticeCreateRequestDto 변경
                                                  @AuthenticationPrincipal UserDetails userDetails) { // ⭐ UserDetails 사용
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ⭐ SecurityUtil.getCurrentUserId() 대신 userDetails.getUsername() 사용
        NoticeDto newNotice = noticeService.createNotice(requestDto, userDetails.getUsername()); // ⭐ NoticeService 메서드 변경에 맞춤
        return ResponseEntity.status(HttpStatus.CREATED).body(newNotice);
    }

    // 4. 게시글 수정 (ROLE_ADMIN 권한 필요)
    // PUT /api/notices/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticeDto> updateNotice(@PathVariable(name = "id") Long id, // ⭐ 반환 타입 Notice -> NoticeDto
                                                 @RequestBody NoticeCreateRequestDto requestDto) { // ⭐ Map -> NoticeCreateRequestDto 변경
        try {
            NoticeDto updatedNotice = noticeService.updateNotice(id, requestDto); // ⭐ NoticeService 메서드 변경에 맞춤
            return ResponseEntity.ok(updatedNotice);
        } catch (EntityNotFoundException e) { // ⭐ IllegalArgumentException -> EntityNotFoundException
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 5. 게시글 삭제 (ROLE_ADMIN 권한 필요)
    // DELETE /api/notices/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(@PathVariable(name = "id") Long id) {
        try {
            noticeService.deleteNotice(id);
            return ResponseEntity.noContent().build(); // 204 No Content 응답
        } catch (EntityNotFoundException e) { // ⭐ IllegalArgumentException -> EntityNotFoundException
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 예외 처리 (NoticeService에서 EntityNotFoundException을 던지므로 이것으로 통일)
    @ExceptionHandler(EntityNotFoundException.class) // ⭐ IllegalArgumentException -> EntityNotFoundException
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException e) {
        Map<String, String> errorResponse = new HashMap();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 Not Found
    }
}