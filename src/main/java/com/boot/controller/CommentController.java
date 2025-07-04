package com.boot.controller; // 적절한 컨트롤러 패키지 경로로 변경

import com.boot.domain.User; // User 엔티티 경로에 맞게 수정
import com.boot.dto.CommentRequestDto; // CommentRequestDto 임포트
import com.boot.dto.CommentResponseDto; // CommentResponseDto 임포트
import com.boot.service.CommentService; // CommentService 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts/{postId}/comments") // 게시글 ID 아래에 댓글 API 경로 정의
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * 특정 게시글에 새로운 댓글 또는 대댓글을 생성합니다.
     * POST /api/posts/{postId}/comments
     *
     * @param postId 댓글이 달릴 게시글의 ID
     * @param requestDto 댓글 요청 DTO (내용, 부모 댓글 ID 포함)
     * @param currentUser 현재 인증된 사용자 정보
     * @return 생성된 댓글의 응답 DTO
     */
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) { // User 엔티티를 직접 principal로 받음
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 댓글 생성 시도: 게시글 ID={}", postId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
        log.info("댓글 생성 요청 수신: 게시글 ID={}, 작성자 ID={}, 내용={}", postId, currentUser.getId(), requestDto.getContent());
        try {
            CommentResponseDto newComment = commentService.createComment(postId, requestDto, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
        } catch (NoSuchElementException e) {
            log.warn("댓글 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 게시글 또는 부모 댓글을 찾을 수 없음
        } catch (IllegalStateException e) {
            log.warn("댓글 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 삭제된 댓글에 답글 등 잘못된 상태
        } catch (Exception e) {
            log.error("댓글 생성 중 서버 오류: 게시글 ID={}, 작성자 ID={}", postId, currentUser.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 특정 게시글의 모든 댓글을 조회합니다. (계층 구조 포함)
     * GET /api/posts/{postId}/comments
     *
     * @param postId 댓글을 조회할 게시글의 ID
     * @return 계층 구조로 구성된 댓글 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPostId(@PathVariable("postId") Long postId) {
        log.info("댓글 목록 조회 요청 수신: 게시글 ID={}", postId);
        try {
            List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
            log.info("댓글 목록 조회 완료: 게시글 ID={}, 댓글 수={}", postId, comments.size());
            return ResponseEntity.ok(comments);
        } catch (NoSuchElementException e) {
            log.warn("댓글 조회 실패: 게시글을 찾을 수 없습니다 (ID={})", postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("댓글 목록 조회 중 서버 오류: 게시글 ID={}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 특정 댓글을 삭제합니다. (소프트 삭제)
     * DELETE /api/posts/{postId}/comments/{commentId}
     *
     * @param postId 댓글이 속한 게시글의 ID
     * @param commentId 삭제할 댓글의 ID
     * @param currentUser 현재 인증된 사용자 정보
     * @return 성공 시 204 No Content
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 댓글 삭제 시도: 게시글 ID={}, 댓글 ID={}", postId, commentId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("댓글 삭제 요청 수신: 게시글 ID={}, 댓글 ID={}, 요청 사용자 ID={}", postId, commentId, currentUser.getId());
        try {
            commentService.deleteComment(postId, commentId, currentUser.getId());
            log.info("댓글 삭제 성공: ID={}", commentId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NoSuchElementException e) {
            log.warn("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 댓글을 찾을 수 없음
        } catch (SecurityException e) {
            log.warn("댓글 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
        } catch (IllegalArgumentException e) {
            log.warn("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 댓글이 게시글에 속하지 않음
        } catch (Exception e) {
            log.error("댓글 삭제 중 서버 오류: 댓글 ID={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
