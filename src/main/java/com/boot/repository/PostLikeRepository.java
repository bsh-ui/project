package com.boot.repository; // 적절한 리포지토리 패키지 경로로 변경

import com.boot.domain.Post; // Post 엔티티 임포트
import com.boot.domain.PostLike; // PostLike 엔티티 임포트
import com.boot.domain.User; // User 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // ⭐ Query 어노테이션 임포트 ⭐
import org.springframework.data.repository.query.Param; // ⭐ Param 어노테이션 임포트 ⭐
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 게시글과 특정 사용자에 대한 좋아요/싫어요 기록을 찾는 메서드
    Optional<PostLike> findByPostAndUser(Post post, User user);

    // ⭐ 중요: @Query 어노테이션을 사용하여 좋아요 개수 쿼리를 명시적으로 정의 ⭐
    // JPQL(Java Persistence Query Language)을 사용하여 명확하게 'pl.isLike' 필드를 참조합니다.
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post AND pl.isLike = :isLikeValue")
    Long countByPostAndIsLike(@Param("post") Post post, @Param("isLikeValue") Boolean isLikeValue);

    // ⭐ 중요: @Query 어노테이션을 사용하여 존재 여부 쿼리를 명시적으로 정의 ⭐
    // 이 쿼리는 특정 게시글, 사용자, 좋아요/싫어요 상태를 가진 PostLike 엔티티가 존재하는지 확인합니다.
    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN TRUE ELSE FALSE END FROM PostLike pl WHERE pl.post = :post AND pl.user = :user AND pl.isLike = :isLikeValue")
    boolean existsByPostAndUserAndIsLike(@Param("post") Post post, @Param("user") User user, @Param("isLikeValue") Boolean isLikeValue);
}
