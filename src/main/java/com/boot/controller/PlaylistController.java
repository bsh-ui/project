package com.boot.controller;

import com.boot.domain.User; // User 엔티티 임포트
import com.boot.dto.PlaylistDTO;
import com.boot.dto.PlaylistDTO.AddMusicRequest;
import com.boot.dto.PlaylistDTO.PlaylistResponse;
import com.boot.dto.UserDTO;
// import com.boot.security.CustomUserDetails; // CustomUserDetails 임포트 제거
import com.boot.service.PlaylistService;
import com.boot.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists") // 플레이리스트 관련 API의 기본 경로
@RequiredArgsConstructor
@Slf4j
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserService userService;

    /**
     * 새로운 플레이리스트를 생성하는 API 엔드포인트.
     * 인증된 사용자만 접근할 수 있습니다.
     */
    @PostMapping
    public ResponseEntity<?> createPlaylist(
            @RequestBody PlaylistDTO.CreateRequest createRequest,
            @AuthenticationPrincipal User user) { // CustomUserDetails 대신 User로 변경

        if (user == null) { // customUserDetails 대신 user로 변경
            log.warn("인증되지 않은 사용자의 플레이리스트 생성 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        Long currentUserId = user.getId(); // customUserDetails.getId() 대신 user.getId()로 변경

        try {
            PlaylistResponse createdPlaylist = playlistService.createPlaylist(createRequest, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist); // 201 Created
        } catch (IllegalArgumentException e) {
            log.error("플레이리스트 생성 실패 - 유효하지 않은 요청 또는 사용자: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        } catch (Exception e) {
            log.error("플레이리스트 생성 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }

    /**
     * 현재 로그인된 사용자의 플레이리스트 목록을 조회하는 API 엔드포인트.
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPlaylists(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("내 플레이리스트 목록 조회 요청: 사용자={}", username);

            // UserDTO user = userService.getUserByUsername(username);
            // 인증 객체에서 직접 User 엔티티를 가져올 수 있다면 UserDTO 변환 없이 바로 사용 가능
            // 하지만 Authentication authentication으로 받은 경우, principal은 UserDetails 타입이므로
            // 실제 User 엔티티를 가져오려면 캐스팅 또는 UserDetailsService를 통한 재조회가 필요할 수 있습니다.
            // 여기서는 기존 로직 유지 (UserService를 통해 UserDTO를 가져오는 방식)
            UserDTO user = userService.getUserByUsername(username); 
            if (user == null) {
                log.error("인증된 사용자({})의 UserDTO를 찾을 수 없습니다.", username);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("사용자 정보를 가져오는 데 실패했습니다."));
            }

            List<PlaylistResponse> myPlaylists = playlistService.getPlaylistsByUserId(user.getId());
            log.info("내 플레이리스트 목록 조회 성공: 사용자={}, 개수={}", username, myPlaylists.size());
            return ResponseEntity.ok(myPlaylists);
        } catch (Exception e) {
            log.error("내 플레이리스트 목록 조회 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("내 플레이리스트 목록을 불러오는 데 실패했습니다."));
        }
    }

    /**
     * 특정 플레이리스트 상세 조회 API 엔드포인트.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaylistDetail(@PathVariable("id") Long playlistId) {
        try {
            PlaylistResponse playlist = playlistService.getPlaylistById(playlistId);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            log.error("플레이리스트 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("플레이리스트 상세 조회 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("플레이리스트 상세 정보를 불러오는 데 실패했습니다."));
        }
    }

    /**
     * 플레이리스트를 수정하는 API 엔드포인트. (소유자만 가능)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable("id") Long playlistId,
            @RequestBody PlaylistDTO.UpdateRequest updateRequest,
            @AuthenticationPrincipal User user) { // CustomUserDetails 대신 User로 변경

        if (user == null) { // customUserDetails 대신 user로 변경
            log.warn("인증되지 않은 사용자의 플레이리스트 수정 시도 (ID: {})", playlistId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
        }

        Long currentUserId = user.getId(); // customUserDetails.getId() 대신 user.getId()로 변경

        try {
            PlaylistResponse updatedPlaylist = playlistService.updatePlaylist(playlistId, updateRequest, currentUserId);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (IllegalArgumentException e) {
            log.error("플레이리스트 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.error("플레이리스트 수정 실패: 권한 없음 (ID: {})", playlistId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("플레이리스트 수정 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("플레이리스트 수정 중 오류가 발생했습니다."));
        }
    }

    /**
     * 플레이리스트를 삭제하는 API 엔드포인트. (소유자만 가능)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(
            @PathVariable("id") Long playlistId,
            @AuthenticationPrincipal User user) { // CustomUserDetails 대신 User로 변경

        if (user == null) { // customUserDetails 대신 user로 변경
            log.warn("인증되지 않은 사용자의 플레이리스트 삭제 시도 (ID: {})", playlistId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
        }

        Long currentUserId = user.getId(); // customUserDetails.getId() 대신 user.getId()로 변경

        try {
            playlistService.deletePlaylist(playlistId, currentUserId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            log.error("플레이리스트 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.error("플레이리스트 삭제 실패: 권한 없음 (ID: {})", playlistId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("플레이리스트 삭제 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("플레이리스트 삭제 중 오류가 발생했습니다."));
        }
    }

    /**
     * 플레이리스트에 음악을 추가하는 API 엔드포인트.
     * URL 예시: POST /api/playlists/{id}/music
     */
    @PostMapping("/{id}/music")
    public ResponseEntity<?> addMusicToPlaylist(
            @PathVariable("id") Long playlistId,
            @RequestBody AddMusicRequest addMusicRequest,
            @AuthenticationPrincipal User user) { // CustomUserDetails 대신 User로 변경

        if (user == null) { // customUserDetails 대신 user로 변경
            log.warn("인증되지 않은 사용자의 플레이리스트 ID {}에 음악 추가 시도", playlistId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
        }

        Long currentUserId = user.getId(); // customUserDetails.getId() 대신 user.getId()로 변경
        Long musicId = addMusicRequest.getMusicId();

        try {
            PlaylistResponse updatedPlaylist = playlistService.addMusicToPlaylist(playlistId, musicId, currentUserId);
            return ResponseEntity.ok(updatedPlaylist); // 200 OK, 업데이트된 플레이리스트 정보 반환
        } catch (IllegalArgumentException e) {
            log.error("플레이리스트 ID {}에 음악 ID {} 추가 실패: {}", playlistId, musicId, e.getMessage());
            return ResponseEntity.badRequest().body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.error("플레이리스트 ID {}에 음악 ID {} 추가 실패: 권한 없음", playlistId, musicId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("플레이리스트 ID {}에 음악 ID {} 추가 중 서버 오류 발생", playlistId, musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PlaylistDTO.ErrorResponse("플레이리스트에 음악 추가 중 오류가 발생했습니다."));
        }
    }

    /**
     * 플레이리스트에서 음악을 삭제하는 API 엔드포인트.
     * URL 예시: DELETE /api/playlists/{playlistId}/music/{musicId}
     */
    @DeleteMapping("/{playlistId}/music/{musicId}")
    public ResponseEntity<?> removeMusicFromPlaylist(
            @PathVariable("playlistId") Long playlistId,
            @PathVariable("musicId") Long musicId,
            @AuthenticationPrincipal User user) { // CustomUserDetails 대신 User로 변경

        if (user == null) { // customUserDetails 대신 user로 변경
            log.warn("인증되지 않은 사용자의 플레이리스트 ID {}에서 음악 ID {} 삭제 시도", playlistId, musicId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
        }

        Long currentUserId = user.getId(); // customUserDetails.getId() 대신 user.getId()로 변경

        try {
            playlistService.removeMusicFromPlaylist(playlistId, musicId, currentUserId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            log.error("플레이리스트 ID {}에서 음악 ID {} 삭제 실패: {}", playlistId, musicId, e.getMessage());
            return ResponseEntity.badRequest().body(new PlaylistDTO.ErrorResponse(e.getMessage())); // 400 Bad Request
        } catch (SecurityException e) {
            log.error("플레이리스트 ID {}에서 음악 ID {} 삭제 실패: 권한 없음", playlistId, musicId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage())); // 403 Forbidden
        } catch (Exception e) {
            log.error("플레이리스트 ID {}에서 음악 ID {} 삭제 중 서버 오류 발생", playlistId, musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PlaylistDTO.ErrorResponse("플레이리스트에서 음악 삭제 중 오류가 발생했습니다."));
        }
    }
}

//package com.boot.controller;
//
//import com.boot.dto.PlaylistDTO;
//import com.boot.dto.PlaylistDTO.AddMusicRequest;
//import com.boot.dto.PlaylistDTO.PlaylistResponse;
//import com.boot.dto.UserDTO;
//import com.boot.security.CustomUserDetails; // CustomUserDetails 임포트 유지
//import com.boot.service.PlaylistService;
//import com.boot.service.UserService;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.List;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/playlists") // 플레이리스트 관련 API의 기본 경로
//@RequiredArgsConstructor
//@Slf4j
//public class PlaylistController {
//
//    private final PlaylistService playlistService;
//    private final UserService userService;
//
//    /**
//     * 새로운 플레이리스트를 생성하는 API 엔드포인트.
//     * 인증된 사용자만 접근할 수 있습니다.
//     */
//    @PostMapping
//    public ResponseEntity<?> createPlaylist(
//            @RequestBody PlaylistDTO.CreateRequest createRequest,
//            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
//
//        if (customUserDetails == null) {
//            log.warn("인증되지 않은 사용자의 플레이리스트 생성 시도");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
//        }
//
//        Long currentUserId = customUserDetails.getId();
//
//        try {
//            PlaylistResponse createdPlaylist = playlistService.createPlaylist(createRequest, currentUserId);
//            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist); // 201 Created
//        } catch (IllegalArgumentException e) {
//            log.error("플레이리스트 생성 실패 - 유효하지 않은 요청 또는 사용자: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(null); // 400 Bad Request
//        } catch (Exception e) {
//            log.error("플레이리스트 생성 중 서버 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
//        }
//    }
//
//    /**
//     * 현재 로그인된 사용자의 플레이리스트 목록을 조회하는 API 엔드포인트.
//     */
//    @GetMapping("/my")
//    public ResponseEntity<?> getMyPlaylists(Authentication authentication) {
//        try {
//            String username = authentication.getName();
//            log.info("내 플레이리스트 목록 조회 요청: 사용자={}", username);
//
//            UserDTO user = userService.getUserByUsername(username);
//            if (user == null) {
//                log.error("인증된 사용자({})의 UserDTO를 찾을 수 없습니다.", username);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("사용자 정보를 가져오는 데 실패했습니다."));
//            }
//
//            List<PlaylistResponse> myPlaylists = playlistService.getPlaylistsByUserId(user.getId());
//            log.info("내 플레이리스트 목록 조회 성공: 사용자={}, 개수={}", username, myPlaylists.size());
//            return ResponseEntity.ok(myPlaylists);
//        } catch (Exception e) {
//            log.error("내 플레이리스트 목록 조회 중 서버 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("내 플레이리스트 목록을 불러오는 데 실패했습니다."));
//        }
//    }
//
//    /**
//     * 특정 플레이리스트 상세 조회 API 엔드포인트.
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getPlaylistDetail(@PathVariable("id") Long playlistId) {
//        try {
//            PlaylistResponse playlist = playlistService.getPlaylistById(playlistId);
//            return ResponseEntity.ok(playlist);
//        } catch (IllegalArgumentException e) {
//            log.error("플레이리스트 상세 조회 실패: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (Exception e) {
//            log.error("플레이리스트 상세 조회 중 서버 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("플레이리스트 상세 정보를 불러오는 데 실패했습니다."));
//        }
//    }
//
//    /**
//     * 플레이리스트를 수정하는 API 엔드포인트. (소유자만 가능)
//     */
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updatePlaylist(
//            @PathVariable("id") Long playlistId,
//            @RequestBody PlaylistDTO.UpdateRequest updateRequest,
//            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
//
//        if (customUserDetails == null) {
//            log.warn("인증되지 않은 사용자의 플레이리스트 수정 시도 (ID: {})", playlistId);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
//        }
//
//        Long currentUserId = customUserDetails.getId();
//
//        try {
//            PlaylistResponse updatedPlaylist = playlistService.updatePlaylist(playlistId, updateRequest, currentUserId);
//            return ResponseEntity.ok(updatedPlaylist);
//        } catch (IllegalArgumentException e) {
//            log.error("플레이리스트 수정 실패: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (SecurityException e) {
//            log.error("플레이리스트 수정 실패: 권한 없음 (ID: {})", playlistId);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (Exception e) {
//            log.error("플레이리스트 수정 중 서버 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("플레이리스트 수정 중 오류가 발생했습니다."));
//        }
//    }
//
//    /**
//     * 플레이리스트를 삭제하는 API 엔드포인트. (소유자만 가능)
//     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deletePlaylist(
//            @PathVariable("id") Long playlistId,
//            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
//
//        if (customUserDetails == null) {
//            log.warn("인증되지 않은 사용자의 플레이리스트 삭제 시도 (ID: {})", playlistId);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
//        }
//
//        Long currentUserId = customUserDetails.getId();
//
//        try {
//            playlistService.deletePlaylist(playlistId, currentUserId);
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
//        } catch (IllegalArgumentException e) {
//            log.error("플레이리스트 삭제 실패: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (SecurityException e) {
//            log.error("플레이리스트 삭제 실패: 권한 없음 (ID: {})", playlistId);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (Exception e) {
//            log.error("플레이리스트 삭제 중 서버 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PlaylistDTO.ErrorResponse("플레이리스트 삭제 중 오류가 발생했습니다."));
//        }
//    }
//
//    /**
//     * 플레이리스트에 음악을 추가하는 API 엔드포인트.
//     * URL 예시: POST /api/playlists/{id}/music
//     */
//    @PostMapping("/{id}/music")
//    public ResponseEntity<?> addMusicToPlaylist(
//            @PathVariable("id") Long playlistId,
//            @RequestBody AddMusicRequest addMusicRequest,
//            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
//
//        if (customUserDetails == null) {
//            log.warn("인증되지 않은 사용자의 플레이리스트 ID {}에 음악 추가 시도", playlistId);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
//        }
//
//        Long currentUserId = customUserDetails.getId();
//        Long musicId = addMusicRequest.getMusicId();
//
//        try {
//            PlaylistResponse updatedPlaylist = playlistService.addMusicToPlaylist(playlistId, musicId, currentUserId);
//            return ResponseEntity.ok(updatedPlaylist); // 200 OK, 업데이트된 플레이리스트 정보 반환
//        } catch (IllegalArgumentException e) {
//            log.error("플레이리스트 ID {}에 음악 ID {} 추가 실패: {}", playlistId, musicId, e.getMessage());
//            return ResponseEntity.badRequest().body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (SecurityException e) {
//            log.error("플레이리스트 ID {}에 음악 ID {} 추가 실패: 권한 없음", playlistId, musicId);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage()));
//        } catch (Exception e) {
//            log.error("플레이리스트 ID {}에 음악 ID {} 추가 중 서버 오류 발생", playlistId, musicId, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new PlaylistDTO.ErrorResponse("플레이리스트에 음악 추가 중 오류가 발생했습니다."));
//        }
//    }
//
//    /**
//     * 플레이리스트에서 음악을 삭제하는 API 엔드포인트.
//     * URL 예시: DELETE /api/playlists/{playlistId}/music/{musicId}
//     */
//    @DeleteMapping("/{playlistId}/music/{musicId}")
//    public ResponseEntity<?> removeMusicFromPlaylist(
//            @PathVariable("playlistId") Long playlistId,
//            @PathVariable("musicId") Long musicId,
//            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
//
//        if (customUserDetails == null) {
//            log.warn("인증되지 않은 사용자의 플레이리스트 ID {}에서 음악 ID {} 삭제 시도", playlistId, musicId);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new PlaylistDTO.ErrorResponse("로그인이 필요합니다."));
//        }
//
//        Long currentUserId = customUserDetails.getId();
//
//        try {
//            playlistService.removeMusicFromPlaylist(playlistId, musicId, currentUserId);
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
//        } catch (IllegalArgumentException e) {
//            log.error("플레이리스트 ID {}에서 음악 ID {} 삭제 실패: {}", playlistId, musicId, e.getMessage());
//            return ResponseEntity.badRequest().body(new PlaylistDTO.ErrorResponse(e.getMessage())); // 400 Bad Request
//        } catch (SecurityException e) {
//            log.error("플레이리스트 ID {}에서 음악 ID {} 삭제 실패: 권한 없음", playlistId, musicId);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PlaylistDTO.ErrorResponse(e.getMessage())); // 403 Forbidden
//        } catch (Exception e) {
//            log.error("플레이리스트 ID {}에서 음악 ID {} 삭제 중 서버 오류 발생", playlistId, musicId, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new PlaylistDTO.ErrorResponse("플레이리스트에서 음악 삭제 중 오류가 발생했습니다."));
//        }
//    }
//}