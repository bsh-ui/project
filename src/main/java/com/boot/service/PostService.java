package com.boot.service; // 적절한 서비스 패키지 경로로 변경

import com.boot.domain.Post; // Post 엔티티 경로에 맞게 수정
import com.boot.domain.User;   // User 엔티티 경로에 맞게 수정
import com.boot.repository.PostRepository; // PostRepository 경로에 맞게 수정
import com.boot.repository.UserRepository; // UserRepository 경로에 맞게 수정
import com.boot.repository.PostLikeRepository; // ⭐ 추가: PostLikeRepository 임포트 ⭐
import com.boot.dto.PostRequestDto; // PostRequestDto 경로에 맞게 수정
import com.boot.dto.PostResponseDto; // PostResponseDto 경로에 맞게 수정
import com.boot.dto.CommentResponseDto; // ⭐ 추가: CommentResponseDto 임포트 ⭐
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // 로깅을 위해 Lombok @Slf4j 추가
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository; // 작성자(User)를 찾기 위한 UserRepository
    private final PostLikeRepository postLikeRepository; // ⭐ 추가: 좋아요 리포지토리 주입 ⭐
    private final CommentService commentService; // ⭐ 추가: 댓글 서비스 주입 ⭐

    // 게시글 생성
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("작성자를 찾을 수 없습니다: " + authorId));

        Post post = new Post(requestDto.getTitle(), requestDto.getContent(), author);
        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료: {}", savedPost.getId());
        // 새 게시글 생성 시에는 좋아요/싫어요/댓글이 없으므로 기본 PostResponseDto 반환
        return new PostResponseDto(savedPost);
    }

    // 모든 게시글 조회 (페이지네이션)
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findByIsDeletedFalse(pageable); // isDeleted가 false인 게시글만 조회
        log.info("모든 게시글 조회 완료 (페이지: {}, 사이즈: {}): {}개", pageable.getPageNumber(), pageable.getPageSize(), postsPage.getTotalElements());

        // 각 Post 엔티티를 PostResponseDto로 변환하면서 좋아요/싫어요 개수와 댓글 개수를 채워 넣습니다.
        return postsPage.map(post -> {
            PostResponseDto dto = new PostResponseDto(post);
            dto.setLikesCount(postLikeRepository.countByPostAndIsLike(post, true)); // 좋아요 개수 설정
            dto.setDislikesCount(postLikeRepository.countByPostAndIsLike(post, false)); // 싫어요 개수 설정
            dto.setComments(commentService.getCommentsByPostId(post.getId())); // 댓글 목록 설정 (계층형)
            // 여기서는 commentService.getCommentsByPostId()가 계층형 DTO 목록을 반환한다고 가정합니다.
            return dto;
        });
    }

    // 특정 게시글 상세 조회 (조회수 증가 및 좋아요/싫어요/댓글 데이터 포함)
    @Transactional
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + id));

        if (post.getIsDeleted()) {
            throw new NoSuchElementException("삭제된 게시글입니다: " + id);
        }

        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post); // 변경사항 저장
        log.info("게시글 조회 완료 및 조회수 증가 (ID: {}), 현재 조회수: {}", id, post.getViewCount());

        // ⭐ PostResponseDto에 좋아요/싫어요 개수 및 댓글 목록 설정 ⭐
        PostResponseDto responseDto = new PostResponseDto(post);
        responseDto.setLikesCount(postLikeRepository.countByPostAndIsLike(post, true)); // 좋아요 개수 설정
        responseDto.setDislikesCount(postLikeRepository.countByPostAndIsLike(post, false)); // 싫어요 개수 설정
        
        // 게시글의 댓글 목록을 가져와 DTO로 변환하여 설정 (계층 구조 포함)
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(post.getId());
        responseDto.setComments(comments);

        return responseDto;
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto requestDto, Long userId) { // userId는 인증된 사용자 ID
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + id));

        if (post.getIsDeleted()) {
            throw new IllegalStateException("삭제된 게시글은 수정할 수 없습니다.");
        }

        // 작성자만 수정 가능하도록 검증 (또는 관리자 권한)
        if (!post.getAuthor().getId().equals(userId)) {
            log.warn("게시글 수정 권한 없음: 게시글 작성자 ID {} != 요청 사용자 ID {}", post.getAuthor().getId(), userId);
            throw new SecurityException("게시글을 수정할 권한이 없습니다.");
        }

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        Post updatedPost = postRepository.save(post);
        log.info("게시글 수정 완료 (ID: {})", updatedPost.getId());
        
        // 수정된 게시글 반환 시에도 좋아요/싫어요/댓글 정보 포함
        PostResponseDto responseDto = new PostResponseDto(updatedPost);
        responseDto.setLikesCount(postLikeRepository.countByPostAndIsLike(updatedPost, true));
        responseDto.setDislikesCount(postLikeRepository.countByPostAndIsLike(updatedPost, false));
        responseDto.setComments(commentService.getCommentsByPostId(updatedPost.getId()));
        return responseDto;
    }

    // 게시글 삭제 (소프트 삭제)
    @Transactional
    public void deletePost(Long id, Long userId) { // userId는 인증된 사용자 ID
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + id));

        if (post.getIsDeleted()) {
            log.warn("이미 삭제된 게시글 재삭제 시도 (ID: {})", id);
            return; // 이미 삭제된 게시글은 다시 처리할 필요 없음
        }

        // 작성자만 삭제 가능하도록 검증 (또는 관리자 권한)
        if (!post.getAuthor().getId().equals(userId)) {
            log.warn("게시글 삭제 권한 없음: 게시글 작성자 ID {} != 요청 사용자 ID {}", post.getAuthor().getId(), userId);
            throw new SecurityException("게시글을 삭제할 권한이 없습니다.");
        }

        post.setIsDeleted(true); // 소프트 삭제
        postRepository.save(post);
        log.info("게시글 소프트 삭제 완료 (ID: {})", id);
    }
}
