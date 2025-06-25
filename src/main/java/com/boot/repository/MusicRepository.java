package com.boot.repository;

import com.boot.domain.Music;
import com.boot.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    // 음악 제목으로 검색 (일부 일치, 대소문자 구분 없음)
    Page<Music> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 아티스트 이름으로 검색 (일부 일치, 대소문자 구분 없음)
    Page<Music> findByArtistContainingIgnoreCase(String artist, Pageable pageable);

    // 특정 업로더가 업로드한 음악 조회
    List<Music> findByUploaderId(Long  uploader);

    // 가장 많이 재생된 음악 N개 조회 (예시: playCount 기준 내림차순)
    List<Music> findTop100ByOrderByPlayCountDesc();
}