package com.boot.service; // 적절한 서비스 패키지 경로로 변경

import com.boot.domain.Comment; // Comment 엔티티 임포트
import com.boot.domain.Post;    // Post 엔티티 임포트
import com.boot.domain.User;    // User 엔티티 임포트
import com.boot.repository.CommentRepository; // CommentRepository 임포트
import com.boot.repository.PostRepository;    // PostRepository 임포트
import com.boot.repository.UserRepository;    // UserRepository 임포트
import com.boot.dto.CommentRequestDto; // CommentRequestDto 임포트
import com.boot.dto.CommentResponseDto; // CommentResponseDto 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 댓글 또는 대댓글을 생성합니다.
     *
     * @param postId 댓글이 달릴 게시글의 ID
     * @param requestDto 댓글 요청 DTO (내용, 부모 댓글 ID 포함)
     * @param authorId 댓글 작성자의 ID
     * @return 생성된 댓글의 응답 DTO
     */
    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto requestDto, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + postId));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("작성자를 찾을 수 없습니다: " + authorId));

        Comment parentComment = null;
        if (requestDto.getParentId() != null) {
            parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new NoSuchElementException("부모 댓글을 찾을 수 없습니다: " + requestDto.getParentId()));
            if (parentComment.getIsDeleted()) {
                throw new IllegalStateException("삭제된 댓글에는 답글을 달 수 없습니다.");
            }
        }

        Comment comment = new Comment(requestDto.getContent(), post, author, parentComment);
        Comment savedComment = commentRepository.save(comment);

        log.info("댓글 생성 완료: ID={}, 게시글 ID={}, 작성자 ID={}, 부모 댓글 ID={}",
                 savedComment.getId(), postId, authorId, requestDto.getParentId());

        return new CommentResponseDto(savedComment);
    }

    /**
     * 특정 게시글의 모든 댓글을 계층 구조로 조회합니다.
     * 삭제된 댓글은 제외하고, 최상위 댓글부터 자식 댓글까지 시간순으로 정렬합니다.
     *
     * @param postId 댓글을 조회할 게시글의 ID
     * @return 계층 구조로 구성된 댓글 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + postId));

        // 해당 게시글의 모든 삭제되지 않은 댓글을 가져옵니다.
        List<Comment> allComments = commentRepository.findByPostAndIsDeletedFalseOrderByCreatedAtAsc(post);

        // Map을 사용하여 댓글 ID를 Comment 엔티티에 매핑
        Map<Long, Comment> commentMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getId, comment -> comment));

        // 계층 구조를 빌드
        List<Comment> rootComments = allComments.stream()
                .filter(comment -> comment.getParent() == null) // 부모가 없는 최상위 댓글만 필터링
                .collect(Collectors.toList());

        // 각 댓글에 자식 댓글 연결
        allComments.forEach(comment -> {
            if (comment.getParent() != null) {
                Comment parent = commentMap.get(comment.getParent().getId());
                if (parent != null) {
                    parent.addChild(comment);
                }
            }
        });

        // 계층 구조를 DTO로 변환
        return rootComments.stream()
                .map(this::convertToCommentDtoRecursive) // 재귀적으로 DTO 변환
                .sorted(Comparator.comparing(CommentResponseDto::getCreatedAt)) // 최상위 댓글 시간순 정렬
                .collect(Collectors.toList());
    }

    // 재귀적으로 Comment 엔티티를 CommentResponseDto로 변환하는 헬퍼 메서드
    private CommentResponseDto convertToCommentDtoRecursive(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setAuthorNickname(comment.getAuthor().getNickname());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setIsDeleted(comment.getIsDeleted());

        // 자식 댓글이 있다면 재귀적으로 DTO로 변환
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            List<CommentResponseDto> childrenDtos = comment.getChildren().stream()
                    .filter(c -> !c.getIsDeleted()) // 삭제되지 않은 자식 댓글만 포함
                    .map(this::convertToCommentDtoRecursive)
                    .sorted(Comparator.comparing(CommentResponseDto::getCreatedAt)) // 자식 댓글도 시간순 정렬
                    .collect(Collectors.toList());
            dto.setChildren(childrenDtos);
        } else {
            dto.setChildren(null); // 자식 댓글이 없으면 null
        }
        return dto;
    }

    /**
     * 댓글을 소프트 삭제합니다.
     *
     * @param postId 댓글이 속한 게시글의 ID
     * @param commentId 삭제할 댓글의 ID
     * @param userId 삭제 요청을 한 사용자의 ID (권한 검증용)
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다: " + commentId));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 댓글 작성자만 삭제 가능하도록 검증 (또는 관리자 권한)
        if (!comment.getAuthor().getId().equals(userId)) {
            log.warn("댓글 삭제 권한 없음: 댓글 작성자 ID {} != 요청 사용자 ID {}", comment.getAuthor().getId(), userId);
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        if (comment.getIsDeleted()) {
            log.warn("이미 삭제된 댓글 재삭제 시도 (ID: {})", commentId);
            return;
        }

        comment.setIsDeleted(true); // 소프트 삭제
        commentRepository.save(comment);
        log.info("댓글 소프트 삭제 완료: ID={}", commentId);
    }
}
