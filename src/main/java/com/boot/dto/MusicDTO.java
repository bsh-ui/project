package com.boot.dto;

import com.boot.domain.Music;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional; // Optional 임포트 (fromEntity에서 Optional.ofNullable 사용시 필요)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicDTO {

    private Long id;
    private String title;
    private List<String> artist;
    private String album;
    private List<String> genre; // Solr schema: multiValued="true"
    private String musicUrl;
    private String coverImagePath;
    private String fileName;
    private Long duration;
    private LocalDateTime uploadDate;
    private Long uploaderId;
    private String uploaderNickname;
    private Long playCount;

    // ⭐ 추가: lyrics 필드 ⭐
    private String lyrics;

    // ⭐ 추가: release_year 필드 (Solr 스키마 및 Music 엔티티에 있음) ⭐
    private Integer releaseYear;

    // ⭐ 추가: category 필드 (Solr 스키마 multiValued="true"에 맞춰 List<String>) ⭐
    private List<String> category;

    // ⭐ 추가: tags 필드 (Solr 스키마 multiValued="true"에 맞춰 List<String>) ⭐
    private List<String> tags;


    public static MusicDTO fromEntity(Music music) {
        // null 체크 및 List<String> 변환 헬퍼
        // Music 엔티티의 필드가 String (콤마 구분)이라고 가정
        List<String> artistList = Optional.ofNullable(music.getArtist())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>()); // 빈 리스트 반환

        List<String> genreList = Optional.ofNullable(music.getGenre())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>());

        List<String> categoryList = Optional.ofNullable(music.getCategory())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>());

        List<String> tagsList = Optional.ofNullable(music.getTags())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>());


        return MusicDTO.builder()
                .id(music.getId())
                .title(music.getTitle())
                .artist(artistList)
                .album(music.getAlbum())
                .genre(genreList)
                .musicUrl("/api/music/stream/" + music.getId())
                .coverImagePath(music.getCoverImagePath())
                .fileName(music.getFileName())
                .duration(music.getDuration())
                .uploadDate(music.getUploadDate())
                .uploaderId(music.getUploaderId())
                .uploaderNickname(music.getUploaderNickname())
                .playCount(music.getPlayCount())
                .lyrics(music.getLyrics()) // ⭐ 추가: lyrics 필드 설정 ⭐
                .releaseYear(music.getReleaseYear()) // ⭐ 추가: releaseYear 필드 설정 ⭐
                .category(categoryList) // ⭐ 추가: category 필드 설정 ⭐
                .tags(tagsList)     // ⭐ 추가: tags 필드 설정 ⭐
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadRequest {
        private String title;
        private String artist;
        private String album;
        private String genre;
        // ⭐ 추가: UploadRequest에도 lyrics, releaseYear, category, tags 필드 추가 (클라이언트 요청에 따라 String으로) ⭐
        private String lyrics;
        private Integer releaseYear;
        private String category;
        private String tags;
    }
}