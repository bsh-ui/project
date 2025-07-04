package com.boot.controller; // 적절한 컨트롤러 패키지 경로로 변경

import com.boot.domain.User; // ⭐ User 엔티티 경로에 맞게 수정 ⭐
import com.boot.dto.PostRequestDto; // DTO 경로에 맞게 수정
import com.boot.dto.PostResponseDto; // DTO 경로에 맞게 수정
import com.boot.service.PostService; // 서비스 경로에 맞게 수정
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // Spring Security UserDetails 임포트
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    // 게시글 생성 (POST /api/posts)
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto requestDto,
                                                      @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 게시글 생성 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        // ⭐ 중요: UserDetails에서 실제 User의 ID를 가져옵니다. ⭐
        // User 도메인(`com.boot.domain.User`)이 UserDetails를 구현하고 있으므로, User 객체를 직접 캐스팅하여 ID를 가져옵니다.
        Long authorId = null;
        if (currentUser instanceof User) { // ⭐ User 엔티티 타입으로 캐스팅 ⭐
            authorId = ((User) currentUser).getId();
        } else {
             log.error("AuthenticationPrincipal이 User 타입이 아닙니다. UserDetails 구현체 확인 필요.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("게시글 생성 요청: 제목='{}', 작성자 ID={}", requestDto.getTitle(), authorId);
        PostResponseDto newPost = postService.createPost(requestDto, authorId); // authorId 전달
        return ResponseEntity.status(HttpStatus.CREATED).body(newPost);
    }

    // 게시글 목록 조회 (GET /api/posts?page=0&size=10&sort=createdAt,desc)
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("게시글 목록 조회 요청: 페이지={}, 사이즈={}, 정렬={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<PostResponseDto> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // 특정 게시글 상세 조회 (GET /api/posts/{id})
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable("id") Long id) {
        log.info("게시글 상세 조회 요청: ID={}", id);
        try {
            PostResponseDto post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (NoSuchElementException e) {
            log.warn("게시글을 찾을 수 없습니다: ID={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // 게시글 수정 (PUT /api/posts/{id})
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable("id") Long id,
                                                      @RequestBody PostRequestDto requestDto,
                                                      @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 게시글 수정 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = null;
        if (currentUser instanceof User) { // User 엔티티 타입으로 캐스팅
            userId = ((User) currentUser).getId();
        } else {
            log.error("AuthenticationPrincipal이 User 타입이 아닙니다. UserDetails 구현체 확인 필요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("게시글 수정 요청: ID={}, 사용자 ID={}", id, userId);
        try {
            PostResponseDto updatedPost = postService.updatePost(id, requestDto, userId); // userId 전달
            return ResponseEntity.ok(updatedPost);
        } catch (NoSuchElementException e) {
            log.warn("게시글 수정 실패: 게시글을 찾을 수 없습니다 (ID: {})", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("게시글 수정 권한 없음 (ID: {}, 사용자 ID: {})", id, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalStateException e) {
            log.warn("게시글 수정 실패: 삭제된 게시글 (ID: {})", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 게시글 삭제 (DELETE /api/posts/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id,
                                           @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자 게시글 삭제 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = null;
        if (currentUser instanceof User) { // User 엔티티 타입으로 캐스팅
            userId = ((User) currentUser).getId();
        } else {
            log.error("AuthenticationPrincipal이 User 타입이 아닙니다. UserDetails 구현체 확인 필요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("게시글 삭제 요청: ID={}, 사용자 ID={}", id, userId);
        try {
            postService.deletePost(id, userId); // userId 전달
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("게시글 삭제 실패: 게시글을 찾을 수 없습니다 (ID: {})", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("게시글 삭제 권한 없음 (ID: {}, 사용자 ID: {})", id, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
