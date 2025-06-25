package com.boot.repository;

import com.boot.domain.Playlist;
import com.boot.domain.PlaylistMusic;
import com.boot.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param; // ⭐ @Param 임포트 추가 ⭐

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistMusicRepository extends JpaRepository<PlaylistMusic, Long> {

    // 특정 플레이리스트에 속한 모든 음악들을 순서대로 조회 (Playlist 객체 사용)
    List<PlaylistMusic> findByPlaylistOrderByMusicOrderAsc(Playlist playlist);

    // 특정 플레이리스트에 특정 음악이 존재하는지 확인 (Playlist, Music 객체 사용)
    Optional<PlaylistMusic> findByPlaylistAndMusic(Playlist playlist, Music music);

    // 특정 플레이리스트에서 가장 높은 음악 순서(musicOrder) 값 찾기
    // ⭐ @Param 어노테이션 추가 ⭐
    @Query("SELECT MAX(pm.musicOrder) FROM PlaylistMusic pm WHERE pm.playlist = :playlist")
    Integer findMaxMusicOrderByPlaylist(@Param("playlist") Playlist playlist);

    // 특정 플레이리스트에서 특정 순서 이후의 음악 순서 조정 (음악 삭제 시)
    // ⭐ @Param 어노테이션 추가 ⭐
    @Modifying
    @Transactional
    @Query("UPDATE PlaylistMusic pm SET pm.musicOrder = pm.musicOrder - 1 WHERE pm.playlist = :playlist AND pm.musicOrder > :deletedOrder")
    void decrementMusicOrderGreaterThan(@Param("playlist") Playlist playlist, @Param("deletedOrder") int deletedOrder);


}