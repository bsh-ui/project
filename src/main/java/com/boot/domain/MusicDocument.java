package com.boot.domain; // 실제 패키지명 확인 및 유지

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date; // Solr의 pdate 타입과 매핑하기 위해 java.util.Date 사용
import java.util.List; // multiValued 필드와 매핑

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicDocument {

    // Solr 스키마의 "id" 필드와 매핑될 고유 ID
    private String id; // Solr의 id는 보통 String 타입 (Long을 String으로 변환하여 사용)

    private String title;
    private List<String> artist; // Solr schema: multiValued="true"
    private String album;
    private String lyrics;       // Solr schema: lyrics
    private List<String> genre;  // Solr schema: multiValued="true"
    private Integer releaseYear; // Solr schema: release_year (snake_case)
    private Long playCount;      // Solr schema: play_count (snake_case)
    private List<String> category; // Solr schema: multiValued="true"
    private List<String> tags;     // Solr schema: multiValued="true"
    private String musicUrl;     // Solr schema: music_url (snake_case)
    private Integer duration;    // Solr schema: duration
    private String coverImagePath; // Solr schema: cover_image_path (snake_case)
    private Date uploadDate;     // Solr schema: uploadDate (multiValued="false")

    // ⭐ 추가: Solr 스키마에 새로 추가된 필드들 ⭐
    private Date createdAt;        // Solr schema: created_at (pdate, multiValued="false")
    private Long uploaderId;       // Solr schema: uploader_id (plong)
    private String uploaderNickname; // Solr schema: uploader_nickname (string)
}