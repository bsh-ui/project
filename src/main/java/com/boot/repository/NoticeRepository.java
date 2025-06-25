// src/main/java/com/boot/repository/NoticeRepository.java
package com.boot.repository;

import com.boot.domain.Notice;
import com.boot.domain.NoticeType; // NoticeType 임포트
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // ⭐ List<Notice>를 반환하는 메서드를 사용하지 않는다면 제거 가능

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // ⭐ 1. 특정 타입의 공지사항을 생성일 기준 내림차순으로 페이징하여 조회
    //    NoticeService의 getNoticeList(NoticeType type, Pageable pageable)가 호출하는 메서드
    Page<Notice> findByTypeOrderByCreatedAtDesc(NoticeType type, Pageable pageable);

    // ⭐ 2. 모든 공지사항을 생성일 기준 내림차순으로 페이징하여 조회
    //    NoticeService의 getNoticeList(null, pageable)가 호출하는 메서드
    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // --- 기존에 List를 반환했던 메서드는 현재 Service에서 사용하지 않는다면 삭제하는 것을 권장합니다 ---
    // List<Notice> findByTypeOrderByCreatedAtDesc(String type); // NoticeType Enum 사용으로 대체됨
    // List<Notice> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String titleKeyword, String contentKeyword);
    // (만약 검색 기능을 구현할 예정이라면 Page<Notice>를 반환하는 형태로 재정의하는 것이 좋습니다)

    // --- 중복 제거 ---
    // Page<Notice> findByType(NoticeType type, Pageable pageable); // findByTypeOrderByCreatedAtDesc와 기능 중복. 위 메서드로 충분함.
}