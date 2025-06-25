package com.boot.repository; // 적절한 리포지토리 패키지 경로로 변경

import com.boot.domain.Comment; // Comment 엔티티 임포트
import com.boot.domain.Post; // Post 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글을 조회 (삭제되지 않은 댓글만, 부모 댓글 기준으로 정렬)
    // 계층 구조를 프론트엔드에서 처리하기 위해 모든 댓글을 가져오거나,
    // 최상위 댓글만 가져온 후 children을 EAGER 로딩하거나 별도 쿼리로 가져와야 합니다.
    List<Comment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

    // 특정 게시글의 최상위 댓글만 조회 (parentId가 null인 댓글)
    List<Comment> findByPostAndParentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

    // 특정 부모 댓글의 자식 댓글 조회
    List<Comment> findByParentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parent);

    // 특정 게시글의 전체 댓글 개수 조회 (삭제된 댓글 포함 가능)
    Long countByPost(Post post);

    // 특정 게시글의 삭제되지 않은 댓글 개수 조회
    Long countByPostAndIsDeletedFalse(Post post);
}
