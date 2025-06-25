package com.boot.repository;

import com.boot.domain.Playlist;
import com.boot.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // 특정 사용자가 생성한 플레이리스트 목록 조회
    List<Playlist> findByUser(User user);

    // 특정 사용자가 생성한 플레이리스트를 페이징하여 조회
    Page<Playlist> findByUser(User user, Pageable pageable);

    // 플레이리스트 제목으로 검색 (일부 일치, 대소문자 구분 없음)
    Page<Playlist> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 특정 사용자의 플레이리스트 중 제목으로 검색
    Page<Playlist> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);
    
    List<Playlist> findByUserId(Long userId);

    // 공개 플레이리스트만 찾는 쿼리 메서드 (필요하다면)
    List<Playlist> findByIsPublicTrue();
    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.playlistMusics pm LEFT JOIN FETCH pm.music WHERE p.id = :id")
    Optional<Playlist> findByIdWithMusics(@Param("id") Long id);
}