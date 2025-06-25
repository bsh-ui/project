package com.boot.controller;

import com.boot.dto.UserDTO;
import com.boot.service.FileStorageService;
import com.boot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException; // IOException은 이제 직접적으로 throw되지 않으므로 제거 가능성을 고려
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@Slf4j
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProfileController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    // 현재 로그인된 사용자의 프로필 사진 업로드/수정
    @PostMapping("/upload-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, Authentication authentication) {
        log.info("프로필 사진 업로드 요청 수신: 파일명={}", file.getOriginalFilename());

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("인증되지 않은 사용자의 프로필 사진 업로드 시도.");
            return ResponseEntity.status(401).body("로그인된 사용자 정보가 없습니다.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String usernameOrEmail = userDetails.getUsername();

        try {
            UserDTO currentUser = userService.findUserByUsernameOrEmail(usernameOrEmail);
            if (currentUser == null) {
                log.error("사용자를 찾을 수 없습니다: {}", usernameOrEmail);
                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
            }
            log.info("로그인된 사용자 ID: {}", currentUser.getId());

            // 기존 프로필 사진이 있다면 삭제
            String oldPicturePath = currentUser.getPicture(); // DB에 저장된 전체 경로
            if (oldPicturePath != null && !oldPicturePath.isEmpty()) {
                fileStorageService.deleteFile(oldPicturePath); // FileStorageService.deleteFile은 fullFilePath를 받음
                log.info("기존 프로필 사진 삭제 시도: {}", oldPicturePath);
            }

            // 새로운 파일 저장
            // ⭐ storeFile 메서드가 이제 저장된 파일의 전체 절대 경로를 직접 반환합니다. ⭐
            String newPicturePath = fileStorageService.storeFile(file, "profile"); // "profile" 서브 디렉토리에 저장
            log.info("새로운 프로필 사진 저장 완료: newPicturePath={}", newPicturePath);

            // User 엔티티에 새로운 사진 경로 업데이트 (DB 반영)
            userService.updateUserPicture(currentUser.getId(), newPicturePath);

            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필 사진이 성공적으로 업로드 및 업데이트 되었습니다.");
            // 클라이언트에서 접근할 URL은 실제 파일 시스템 경로가 아닌, 웹에서 접근 가능한 URI여야 합니다.
            // 여기서는 fileName을 기반으로 URI를 구성하는 것이 일반적입니다.
            // newPicturePath에서 실제 파일명만 추출하여 URI에 사용해야 합니다.
            String storedFileName = newPicturePath.substring(newPicturePath.lastIndexOf("/") + 1); // 경로에서 파일명만 추출
            response.put("pictureUrl", "/api/profile/picture/" + storedFileName);
            log.info("프로필 사진 업로드 성공. 반환 URL: {}", response.get("pictureUrl"));
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("프로필 사진 업로드에 실패했습니다: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("프로필 사진 업로드에 실패했습니다: " + ex.getMessage());
        }
    }

    // 프로필 사진 조회 (파일 자체를 반환)
    @GetMapping("/picture/{fileName:.+}")
    public ResponseEntity<Resource> downloadProfilePicture(@PathVariable String fileName, HttpServletRequest request) {
        log.info("프로필 사진 다운로드 요청: fileName={}", fileName);
        try {
            // ⭐ FileStorageService.loadFileAsResource는 fullFilePath를 받습니다. ⭐
            // DB에 저장된 fullFilePath는 "C:/path/to/uploads/profile/uuid-filename.jpg" 형태입니다.
            // 하지만 이 API의 @PathVariable은 "uuid-filename.jpg"와 같은 파일명만 받습니다.
            // 따라서, 이 파일명을 이용하여 해당 사용자의 프로필 사진의 '전체 경로'를 DB에서 조회해야 합니다.
            // UserDTO에 picture 필드에 fullFilePath가 저장되어 있으므로,
            // fileName이 일치하는 User를 찾아서 해당 User의 picture 경로를 넘겨주는 로직이 필요합니다.
            // 현재 UserDTO에는 picture 필드에 전체 경로가 저장되어 있고,
            // 이 API는 fileName만 받으므로, fileName으로 사용자를 직접 찾는 것은 비효율적입니다.
            // 대신, 파일명으로 저장된 경로를 재구성하는 방법으로 변경합니다.
            // fileStorageService.getStoredFilePath는 이제 필요 없는 메서드이지만,
            // 여기서는 웹 경로에서 받은 파일명을 실제 저장 경로로 변환하는 역할로 사용될 수 있습니다.
            // 하지만, 더 정확한 방법은 특정 사용자의 프로필 사진 경로를 직접 조회하는 것입니다.
            // 일단은 `getStoredFilePath`를 사용하여 경로를 재구성하는 방식으로 진행합니다.
            // ⭐ 이 부분은 실제 서비스에서는 userId를 파라미터로 받거나 인증된 사용자 정보를 이용해
            // DB에서 해당 사용자의 picture 경로를 정확히 가져오는 것이 더 안전하고 명확합니다.
            // 현재는 파일명만으로 경로를 재구성하여 로드하는 방식으로 수정합니다. ⭐

            // 'profile' 서브디렉토리와 전달받은 fileName을 조합하여 예상되는 저장 경로를 구성합니다.
            String fullFilePathInStorage = fileStorageService.getFilePath("profile", fileName);

            Resource resource = fileStorageService.loadFileAsResource(fullFilePathInStorage);

            // 파일의 MIME 타입 결정
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.warn("파일({})의 MIME 타입을 결정할 수 없습니다: {}", resource.getFilename(), ex.getMessage());
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            log.info("프로필 사진 로드 성공: 파일명={}, Content-Type={}", resource.getFilename(), contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            log.error("프로필 사진 로드 실패: fileName={}, 오류={}", fileName, ex.getMessage(), ex);
            return ResponseEntity.status(404).body(null);
        }
    }

    // 프로필 사진 삭제
    @DeleteMapping("/delete-picture")
    public ResponseEntity<?> deleteProfilePicture(Authentication authentication) {
        log.info("프로필 사진 삭제 요청 수신.");

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("인증되지 않은 사용자의 프로필 사진 삭제 시도.");
            return ResponseEntity.status(401).body("로그인된 사용자 정보가 없습니다.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String usernameOrEmail = userDetails.getUsername();

        try {
            UserDTO currentUser = userService.findUserByUsernameOrEmail(usernameOrEmail);
            if (currentUser == null) {
                log.error("사용자를 찾을 수 없습니다: {}", usernameOrEmail);
                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
            }

            String pictureToDeletePath = currentUser.getPicture(); // DB에 저장된 전체 경로
            if (pictureToDeletePath == null || pictureToDeletePath.isEmpty()) {
                log.info("삭제할 프로필 사진이 없습니다.");
                return ResponseEntity.ok("삭제할 프로필 사진이 없습니다.");
            }

            // FileStorageService.deleteFile은 fullFilePath를 받음
            if (fileStorageService.deleteFile(pictureToDeletePath)) {
                // DB에서 프로필 사진 URL 필드 null로 업데이트
                userService.updateUserPicture(currentUser.getId(), null);
                log.info("프로필 사진 삭제 성공: {}", pictureToDeletePath);

                Map<String, String> response = new HashMap<>();
                response.put("message", "프로필 사진이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                log.error("프로필 사진 삭제 실패: {}", pictureToDeletePath);
                // 파일 시스템에서 삭제는 실패했으나, DB 업데이트는 하지 않고 오류 응답
                return ResponseEntity.status(500).body("프로필 사진 삭제에 실패했습니다.");
            }

        } catch (Exception ex) {
            log.error("프로필 사진 삭제 중 오류가 발생했습니다: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("프로필 사진 삭제 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }
}

//package com.boot.controller;
//
//import com.boot.dto.UserDTO;
//import com.boot.service.FileStorageService;
//import com.boot.service.UserService;
//import lombok.extern.slf4j.Slf4j; // ⭐ Slf4j 임포트
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import jakarta.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/profile")
//@Slf4j // ⭐ Slf4j 어노테이션 추가
//public class ProfileController {
//
//    private final UserService userService;
//    private final FileStorageService fileStorageService;
//
//    @Autowired
//    public ProfileController(UserService userService, FileStorageService fileStorageService) {
//        this.userService = userService;
//        this.fileStorageService = fileStorageService;
//    }
//
//    // 현재 로그인된 사용자의 프로필 사진 업로드/수정
//    @PostMapping("/upload-picture")
//    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, Authentication authentication) {
//        log.info("프로필 사진 업로드 요청 수신: 파일명={}", file.getOriginalFilename());
//
//        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
//            log.warn("인증되지 않은 사용자의 프로필 사진 업로드 시도.");
//            return ResponseEntity.status(401).body("로그인된 사용자 정보가 없습니다.");
//        }
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String usernameOrEmail = userDetails.getUsername();
//
//        try {
//            UserDTO currentUser = userService.findUserByUsernameOrEmail(usernameOrEmail);
//            if (currentUser == null) {
//                log.error("사용자를 찾을 수 없습니다: {}", usernameOrEmail);
//                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
//            }
//            log.info("로그인된 사용자 ID: {}", currentUser.getId());
//
//            // 기존 프로필 사진이 있다면 삭제
//            String oldPicturePath = currentUser.getPicture(); // DB에 저장된 전체 경로
//            if (oldPicturePath != null && !oldPicturePath.isEmpty()) {
//                // FileStorageService.deleteFile은 fullFilePath를 받도록 변경되었음
//                fileStorageService.deleteFile(oldPicturePath); // ⭐ 변경: fullFilePath 전달 ⭐
//                log.info("기존 프로필 사진 삭제 시도: {}", oldPicturePath);
//            }
//
//            // 새로운 파일 저장
//            // ⭐ storeFile 메서드에 subDir "profile"을 전달 ⭐
//            String uniqueFileName = fileStorageService.storeFile(file, "profile");
//            // ⭐ getStoredFilePath 메서드도 subDir "profile"을 전달 ⭐
//            String newPicturePath = fileStorageService.getStoredFilePath("profile", uniqueFileName); // DB에 저장할 전체 경로
//
//            log.info("새로운 프로필 사진 저장 완료: uniqueFileName={}, newPicturePath={}", uniqueFileName, newPicturePath);
//
//            // User 엔티티에 새로운 사진 경로 업데이트 (DB 반영)
//            userService.updateUserPicture(currentUser.getId(), newPicturePath);
//
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "프로필 사진이 성공적으로 업로드 및 업데이트 되었습니다.");
//            response.put("pictureUrl", "/api/profile/picture/" + uniqueFileName); // 웹에서 접근할 URI (고유 파일명만 노출)
//            log.info("프로필 사진 업로드 성공. 반환 URL: {}", response.get("pictureUrl"));
//            return ResponseEntity.ok(response);
//
//        } catch (Exception ex) {
//            log.error("프로필 사진 업로드에 실패했습니다: {}", ex.getMessage(), ex);
//            return ResponseEntity.status(500).body("프로필 사진 업로드에 실패했습니다: " + ex.getMessage());
//        }
//    }
//
//    // 프로필 사진 조회 (파일 자체를 반환)
//    @GetMapping("/picture/{fileName:.+}")
//    public ResponseEntity<Resource> downloadProfilePicture(@PathVariable String fileName, HttpServletRequest request) {
//        log.info("프로필 사진 다운로드 요청: fileName={}", fileName);
//        try {
//            // ⭐ loadFileAsResource는 fullFilePath를 받도록 변경되었음.
//            // 여기서는 웹 URI에서 추출된 fileName만 있으므로, fileStorageService에서 getStoredFilePath로 전체 경로를 재구성해야 합니다.
//            // 또는 FileStorageService.loadFileAsResource가 fileName만으로도 찾을 수 있도록 내부 로직을 보완해야 합니다.
//            // 현재 FileStorageService의 loadFileAsResource는 fullFilePath를 받으므로,
//            // 이 컨트롤러에서는 fileName을 기반으로 "profile" 서브디렉토리와 조합된 전체 경로를 넘겨야 합니다.
//            String fullFilePathInStorage = fileStorageService.getStoredFilePath("profile", fileName); // ⭐ 변경 ⭐
//
//            Resource resource = fileStorageService.loadFileAsResource(fullFilePathInStorage); // ⭐ 변경: fullFilePathInStorage 전달 ⭐
//
//            // 파일의 MIME 타입 결정
//            String contentType = null;
//            try {
//                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//            } catch (IOException ex) {
//                log.warn("파일({})의 MIME 타입을 결정할 수 없습니다: {}", resource.getFilename(), ex.getMessage());
//            }
//
//            if (contentType == null) {
//                contentType = "application/octet-stream";
//            }
//            log.info("프로필 사진 로드 성공: 파일명={}, Content-Type={}", resource.getFilename(), contentType);
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
//                    .body(resource);
//        } catch (Exception ex) {
//            log.error("프로필 사진 로드 실패: fileName={}, 오류={}", fileName, ex.getMessage(), ex);
//            return ResponseEntity.status(404).body(null); // 파일을 찾을 수 없을 때
//        }
//    }
//
//    // 프로필 사진 삭제
//    @DeleteMapping("/delete-picture")
//    public ResponseEntity<?> deleteProfilePicture(Authentication authentication) {
//        log.info("프로필 사진 삭제 요청 수신.");
//
//        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
//            log.warn("인증되지 않은 사용자의 프로필 사진 삭제 시도.");
//            return ResponseEntity.status(401).body("로그인된 사용자 정보가 없습니다.");
//        }
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String usernameOrEmail = userDetails.getUsername();
//
//        try {
//            UserDTO currentUser = userService.findUserByUsernameOrEmail(usernameOrEmail);
//            if (currentUser == null) {
//                log.error("사용자를 찾을 수 없습니다: {}", usernameOrEmail);
//                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
//            }
//
//            String pictureToDeletePath = currentUser.getPicture(); // DB에 저장된 전체 경로
//            if (pictureToDeletePath == null || pictureToDeletePath.isEmpty()) {
//                log.info("삭제할 프로필 사진이 없습니다.");
//                return ResponseEntity.ok("삭제할 프로필 사진이 없습니다.");
//            }
//
//            // FileStorageService.deleteFile은 fullFilePath를 받도록 변경되었음
//            if (fileStorageService.deleteFile(pictureToDeletePath)) { // ⭐ 변경: fullFilePath 전달 ⭐
//                // DB에서 프로필 사진 URL 필드 null로 업데이트
//                userService.updateUserPicture(currentUser.getId(), null);
//                log.info("프로필 사진 삭제 성공: {}", pictureToDeletePath);
//
//                Map<String, String> response = new HashMap<>();
//                response.put("message", "프로필 사진이 성공적으로 삭제되었습니다.");
//                return ResponseEntity.ok(response);
//            } else {
//                log.error("프로필 사진 삭제 실패: {}", pictureToDeletePath);
//                return ResponseEntity.status(500).body("프로필 사진 삭제에 실패했습니다.");
//            }
//
//        } catch (Exception ex) {
//            log.error("프로필 사진 삭제 중 오류가 발생했습니다: {}", ex.getMessage(), ex);
//            return ResponseEntity.status(500).body("프로필 사진 삭제 중 오류가 발생했습니다: " + ex.getMessage());
//        }
//    }
//}