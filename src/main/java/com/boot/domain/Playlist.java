package com.boot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "playlists") // 테이블명은 'playlists'로 설정
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    // 플레이리스트를 생성한 사용자 (User 엔티티와 ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 'user_id' 컬럼으로 User 테이블과 조인
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    // 플레이리스트에 포함된 음악 목록 (PlaylistMusic 엔티티와 OneToMany 관계)
    // PlaylistMusic 엔티티가 Playlist와 Music을 연결하는 중간 테이블 역할을 합니다.
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistMusic> playlistMusics = new ArrayList<>();

    public Playlist(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }

    // 플레이리스트 정보 업데이트 메서드 (제목, 설명)
    public void updatePlaylist(String title, String description, Boolean isPublic) { // ⭐ isPublic 파라미터 추가 ⭐
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        this.description = description;
        if (isPublic != null) { // isPublic 값도 업데이트 가능하게
            this.isPublic = isPublic;
        }
    }
}