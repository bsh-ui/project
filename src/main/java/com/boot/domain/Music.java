package com.boot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String artist; // DB에는 String으로 저장 (콤마 등으로 구분된 문자열)

    @Column(length = 255)
    private String album;

    @Column(length = 255)
    private String genre; // DB에는 String으로 저장 (콤마 등으로 구분된 문자열)

    // lyrics 필드
    @Column(length = 4000) // 가사 길이에 따라 적절한 길이 설정
    private String lyrics;

    // release_year 필드
    @Column(name = "release_year")
    private Integer releaseYear;

    // category 필드 (DB에는 String으로 저장)
    @Column(length = 255)
    private String category;

    // tags 필드 (DB에는 String으로 저장)
    @Column(length = 255)
    private String tags;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(length = 500)
    private String coverImagePath;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "duration")
    private Long duration; // 음악 길이 (초 단위)

    @CreatedDate
    @Column(name = "upload_date", updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "uploader_id")
    private Long uploaderId;

    // ⭐ 추가: Solr 스키마에 맞춰 uploader_nickname 필드 추가 ⭐
    @Column(name = "uploader_nickname", length = 255)
    private String uploaderNickname;

    @ColumnDefault("0")
    @Column(name = "play_count", nullable = false)
    private Long playCount;

    // 음악 정보 업데이트 메서드 (genres, lyrics, releaseYear, category, tags 추가)
    // uploaderNickname은 일반적으로 이 메서드를 통해 업데이트되지 않으므로 포함하지 않음
    public void updateMusicInfo(String title, String artist, String album, String lyrics,
                                String genre, Integer releaseYear, String category, String tags, Long duration) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lyrics = lyrics;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.category = category;
        this.tags = tags;
        this.duration = duration;
    }

    public void incrementPlayCount() {
        this.playCount++;
    }
}