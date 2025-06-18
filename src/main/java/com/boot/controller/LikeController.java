package com.boot.controller; // 적절한 컨트롤러 패키지 경로로 변경

import com.boot.domain.User; // User 엔티티 경로에 맞게 수정
import com.boot.service.LikeService; // LikeService 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts/{postId}") // 게시글 ID 아래에 좋아요/싫어요 API 경로 정의
@RequiredArgsConstructor
@Slf4j
public class LikeController {

    private final LikeService likeService;

    /**
     * 특정 게시글에 좋아요를 토글(추가/취소/변경)합니다.
     * POST /api/posts/{postId}/likes
     *
     * @param postId 대상 게시글 ID
     * @param currentUser 현재 인증된 사용자 정보
     * @return 성공 여부에 따른 응답 (200 OK 또는 401 Unauthorized 등)
     */
    @PostMapping("/likes")
    public ResponseEntity<Void> toggleLike(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 좋아요 요청 시도: 게시글 ID={}", postId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
        log.info("좋아요 요청 수신: 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId());
        try {
            boolean changed = likeService.toggleLikeDislike(postId, currentUser.getId(), true); // 좋아요 (true)
            if (changed) {
                log.info("좋아요 처리 완료: 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId());
                return ResponseEntity.ok().build(); // 200 OK
            } else {
                log.warn("좋아요 상태 변경 없음 (이미 처리됨): 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId());
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict (선택 사항: 이미 처리된 경우)
            }
        } catch (NoSuchElementException e) {
            log.warn("좋아요 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 게시글 또는 사용자를 찾을 수 없음
        } catch (Exception e) {
            log.error("좋아요 처리 중 서버 오류: 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 게시글에 싫어요를 토글(추가/취소/변경)합니다.
     * POST /api/posts/{postId}/dislikes
     *
     * @param postId 대상 게시글 ID
     * @param currentUser 현재 인증된 사용자 정보
     * @return 성공 여부에 따른 응답 (200 OK 또는 401 Unauthorized 등)
     */
    @PostMapping("/dislikes")
    public ResponseEntity<Void> toggleDislike(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 싫어요 요청 시도: 게시글 ID={}", postId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
        log.info("싫어요 요청 수신: 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId());
        try {
            boolean changed = likeService.toggleLikeDislike(postId, currentUser.getId(), false); // 싫어요 (false)
            if (changed) {
                log.info("싫어요 처리 완료: 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId());
                return ResponseEntity.ok().build(); // 200 OK
            } else {
                log.warn("싫어요 상태 변경 없음 (이미 처리됨): 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId());
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
            }
        } catch (NoSuchElementException e) {
            log.warn("싫어요 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("싫어요 처리 중 서버 오류: 게시글 ID={}, 사용자 ID={}", postId, currentUser.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
