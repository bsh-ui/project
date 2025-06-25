package com.boot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder; // ⭐ 이 import는 그대로 유지 ⭐
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // ⭐ 추가: @AllArgsConstructor 임포트 ⭐
import org.springframework.data.annotation.CreatedDate; // ⭐ 추가: CreatedDate 임포트 ⭐
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // ⭐ 추가: AuditingEntityListener 임포트 ⭐
import java.time.LocalDateTime; // ⭐ 추가: LocalDateTime 임포트 ⭐


@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // ⭐ 추가: 모든 필드를 인자로 받는 생성자 자동 생성 (Builder와 함께 사용 시 편리) ⭐
@Entity
@Table(name = "playlist_music")
@Builder // ⭐ @Builder를 클래스 레벨로 이동 ⭐
@EntityListeners(AuditingEntityListener.class) // ⭐ 추가: CreatedDate 자동 생성을 위해 필요 ⭐
public class PlaylistMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @Column(name = "music_order", nullable = false) // ⭐ 컬럼명도 명시하는 것이 좋음 ⭐
    private Integer musicOrder; // ⭐ int 대신 Integer 사용 (null 가능성, JPA 명세) ⭐

    @CreatedDate // ⭐ 추가: 플레이리스트에 음악이 추가된 시간 자동 기록 ⭐
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt; // 플레이리스트에 추가된 시간

    // public PlaylistMusic(Playlist playlist, Music music, int musicOrder) {
    //     this.playlist = playlist;
    //     this.music = music;
    //     this.musicOrder = musicOrder;
    // }
}