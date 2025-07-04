package com.boot.service; // 적절한 서비스 패키지 경로로 변경

import com.boot.domain.Post; // Post 엔티티 임포트
import com.boot.domain.PostLike; // PostLike 엔티티 임포트
import com.boot.domain.User; // User 엔티티 임포트
import com.boot.repository.PostRepository; // PostRepository 임포트
import com.boot.repository.PostLikeRepository; // PostLikeRepository 임포트
import com.boot.repository.UserRepository; // UserRepository 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시글에 좋아요 또는 싫어요를 추가/변경합니다.
     * 이미 누른 경우 상태를 변경하고, 같은 것을 또 누르면 취소합니다.
     *
     * @param postId 대상 게시글 ID
     * @param userId 좋아요/싫어요를 누른 사용자 ID
     * @param isLike true면 좋아요, false면 싫어요
     * @return 좋아요/싫어요 상태 변경 결과 (true: 성공, false: 실패 또는 상태 변경 없음)
     */
    @Transactional
    public boolean toggleLikeDislike(Long postId, Long userId, boolean isLike) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            PostLike like = existingLike.get();
            if (like.getIsLike().equals(isLike)) {
                // 이미 같은 종류(좋아요/싫어요)를 눌렀다면 취소 (삭제)
                postLikeRepository.delete(like);
                log.info("좋아요/싫어요 취소 완료: Post ID={}, User ID={}, Type={}", postId, userId, isLike ? "좋아요" : "싫어요");
                return true; // 상태가 변경되었음을 알림
            } else {
                // 다른 종류(좋아요 -> 싫어요 또는 싫어요 -> 좋아요)를 눌렀다면 상태 변경
                like.setIsLike(isLike);
                postLikeRepository.save(like);
                log.info("좋아요/싫어요 상태 변경 완료: Post ID={}, User ID={}, From={} To={}", postId, userId, !isLike ? "좋아요" : "싫어요", isLike ? "좋아요" : "싫어요");
                return true; // 상태가 변경되었음을 알림
            }
        } else {
            // 새로 좋아요 또는 싫어요 추가
            PostLike newLike = new PostLike(post, user, isLike);
            postLikeRepository.save(newLike);
            log.info("새로운 좋아요/싫어요 추가 완료: Post ID={}, User ID={}, Type={}", postId, userId, isLike ? "좋아요" : "싫어요");
            return true; // 새로 추가되었음을 알림
        }
    }

    /**
     * 특정 게시글의 좋아요 개수를 조회합니다.
     * @param postId 대상 게시글 ID
     * @return 좋아요 개수
     */
    @Transactional(readOnly = true)
    public Long getLikesCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + postId));
        return postLikeRepository.countByPostAndIsLike(post, true);
    }

    /**
     * 특정 게시글의 싫어요 개수를 조회합니다.
     * @param postId 대상 게시글 ID
     * @return 싫어요 개수
     */
    @Transactional(readOnly = true)
    public Long getDislikesCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + postId));
        return postLikeRepository.countByPostAndIsLike(post, false);
    }
}
