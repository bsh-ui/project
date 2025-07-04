package com.boot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PlaylistDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String title;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaylistResponse {
        private Long id;
        private String title;
        private String description;
        private String userNickname;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean isPublic;
        private List<MusicDTO> musics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private Boolean isPublic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddMusicRequest {
        private Long musicId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
}
//package com.boot.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List; // 플레이리스트에 포함된 음악 목록을 나타내기 위해 필요
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class PlaylistDTO {
//    private Long id;
//    private String title;
//    private String description;
//    private Long userId; // 플레이리스트를 만든 사용자 ID
//    private String username; // 플레이리스트를 만든 사용자 이름 (편의상)
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    
//    // 플레이리스트에 포함된 음악들의 DTO 리스트 (음악 추가/조회 시 사용될 수 있음)
//    // 이 필드는 플레이리스트 생성 시에는 사용되지 않고, 플레이리스트 상세 조회 시 포함될 수 있습니다.
//    private List<MusicDTO> musics; // MusicDTO가 정의되어 있어야 합니다.
//
//    // 플레이리스트 생성 요청 시 사용할 DTO (제목, 설명만 필요)
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class CreateRequest {
//        private String title;
//        private String description;
//    }
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class PlaylistResponse {
//        private Long id;
//        private String title;
//        private String description;
//        private String userNickname; // 생성자 닉네임
//        private LocalDateTime createdAt;
//        private LocalDateTime updatedAt;
//        private Boolean isPublic; // 공개 여부 (새로 추가한 컬럼이라면)
//    }
//    // 오류 응답을 위한 DTO (기존에 있었을 것)
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ErrorResponse {
//        private String message;
//    }
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder // UpdateRequest에도 Builder 패턴 추가
//    public static class UpdateRequest {
//        private String title;
//        private String description;
//        private Boolean isPublic; // 공개 여부도 수정 가능하게 (선택 사항)
//    }
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class AddMusicRequest {
//        private Long musicId;
//        // private Integer orderInPlaylist; // (선택 사항) 음악 순서를 직접 지정할 필요가 있다면 추가
//    }
//
//}