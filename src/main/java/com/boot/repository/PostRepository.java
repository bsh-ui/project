package com.boot.repository; // 적절한 리포지토리 패키지 경로로 변경

import com.boot.domain.Post; // Post 엔티티 경로에 맞게 수정
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // isDeleted 필드가 false인 게시글만 페이지네이션하여 조회
    Page<Post> findByIsDeletedFalse(Pageable pageable);

    // 추가로, 작성자 ID와 isDeleted 상태로 조회하는 메서드가 필요할 수도 있습니다.
    // Page<Post> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);
}
