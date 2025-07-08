package com.boot.service;

import com.boot.domain.PasswordResetToken;
import com.boot.domain.Role;
import com.boot.domain.User; // User 엔티티 임포트
import com.boot.domain.UserDocument;
import com.boot.dto.SignUpRequestDTO;
import com.boot.dto.UserDTO; // UserDTO 임포트
import com.boot.exception.UserNotFoundException;
import com.boot.repository.PasswordResetTokenRepository;
import com.boot.repository.UserRepository;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
// import java.util.Set; // Set은 이제 직접 사용하지 않으므로 임포트 불필요
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final UserSolrService userSolrService;
    /**
     * 일반 회원가입 처리
     * @param signUpRequestDTO 회원가입할 사용자 정보 (SignUpRequestDTO 사용)
     * @return 저장된 사용자 정보 (UserDTO)
     */
    @Transactional
    public UserDTO registerNewUser(SignUpRequestDTO signUpRequestDTO) {
        log.info("회원가입 시도: {}", signUpRequestDTO.getUsername());

        // 1. 아이디(username) 중복 확인
        if (userRepository.findByUsername(signUpRequestDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자 아이디입니다.");
        }
        // 2. 이메일 중복 확인
        if (userRepository.findByEmail(signUpRequestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        // 3. 닉네임 중복 확인 (UserRepository에 findByNickname 메서드 추가 필요)
        if (userRepository.findByNickname(signUpRequestDTO.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequestDTO.getPassword());

        // 5. User 엔티티 생성 및 기본 역할 부여
        User user = User.builder()
                .username(signUpRequestDTO.getUsername())
                .email(signUpRequestDTO.getEmail())
                .password(encodedPassword)
                .nickname(signUpRequestDTO.getNickname())
                .birth(signUpRequestDTO.getBirth())
                .gender(signUpRequestDTO.getGender())
                // 일반 가입 시 picture, provider, providerId는 DTO에 없으므로 null로 설정됨
                .build();
        
        // ⭐ 수정: user.setRoles(Collections.singleton("ROLE_USER")) 대신 getRoles().add() 사용 ⭐
        // User 엔티티의 roles 필드는 List<String>이므로 add 메서드 사용
        user.getRoles().add(Role.ROLE_USER);
        user.setEnabled(true); // 새 사용자는 기본적으로 활성화

        // 6. DB 저장
        User savedUser = userRepository.save(user);
        log.info("새 사용자 등록 완료: {}", savedUser.getUsername());

        userSolrService.indexUserDocument(convertToUserDocument(savedUser));
        
        return savedUser.toDTO();
    }

    /**
     * 사용자 아이디로 사용자 정보를 조회합니다.
     * @param username 조회할 사용자 아이디
     * @return 조회된 사용자 정보 (UserDTO), 없으면 null 반환
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::toDTO)
                .orElse(null);
    }

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     * @param id 조회할 사용자 ID
     * @return 조회된 사용자 정보 (UserDTO), 없으면 null 반환
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(User::toDTO)
                .orElse(null);
    }

    /**
     * OAuth2 로그인 시 사용자 정보를 처리합니다.
     * 이미 존재하는 사용자라면 업데이트하고, 새로운 사용자라면 등록합니다.
     *
     * @param provider OAuth2 제공자 (예: "google", "naver", "kakao")
     * @param providerId OAuth2 제공자의 고유 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param picture 프로필 이미지 URL
     * @return 처리된 사용자 정보 (User 엔티티)
     * ⭐ 반환 타입을 UserDTO에서 User 엔티티로 변경하여 CustomOAuth2UserService의 오류 해결 ⭐
     */
    @Transactional
    public User processOAuthUser(String provider, String providerId, String email, String nickname, String picture) {
        log.info("OAuth2 사용자 처리 시도: provider={}, providerId={}, email={}, nickname={}", provider, providerId, email, nickname);

        Optional<User> existingUserOpt = userRepository.findByProviderAndProviderId(provider, providerId);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // User 엔티티의 update 메서드에 profilePicture 업데이트 로직이 있는지 확인하세요.
            // 없으면 user.setProfilePicture(picture)를 직접 호출해야 합니다.
            user.setEmail(email); // 이메일도 업데이트
            user.setNickname(nickname); // 닉네임 업데이트
            user.setPicture(picture); // 프로필 사진 업데이트
            log.info("기존 OAuth2 사용자 정보 업데이트: {}", user.getUsername());
        } else {
            Optional<User> emailUserOpt = userRepository.findByEmail(email);

            if (emailUserOpt.isPresent()) {
                user = emailUserOpt.get();
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setPicture(picture);
                log.info("기존 이메일 계정에 OAuth2 정보 연동: {}", user.getUsername());
            } else {
                user = User.builder()
                        .username(email) // OAuth2 사용자의 username은 이메일로 설정하는 경우가 많음
                        .email(email)
                        // OAuth2 사용자는 비밀번호가 필요 없으므로 무작위 문자열로 설정
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .nickname(nickname)
                        .picture(picture)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                // ⭐ 오류 수정: User 엔티티의 getRoles()에 직접 "ROLE_USER" 추가 ⭐
//                user.getRoles().add(null); // 새로운 OAuth2 사용자도 기본 ROLE_USER 부여
                user.getRoles().add(Role.ROLE_USER); // 새로운 OAuth2 사용자도 기본 ROLE_USER 부여
                log.info("새로운 OAuth2 사용자 등록: {}", user.getUsername());
            }
        }
        User savedUser = userRepository.save(user);
        userSolrService.indexUserDocument(convertToUserDocument(savedUser));
        return savedUser; // ⭐ UserDTO가 아닌 User 엔티티 자체를 반환 ⭐
    }

    /**
     * 사용자 정보 수정
     * @param userId 수정할 사용자 ID
     * @param userDTO 업데이트할 사용자 정보 (username, email, password 제외)
     * @return 업데이트된 UserDTO
     */
    @Transactional
    public UserDTO updateUserInfo(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 닉네임 중복 체크 로직 추가 (만약 닉네임을 변경하려 할 때)
        if (userDTO.getNickname() != null && !userDTO.getNickname().equals(user.getNickname())) {
            if (userRepository.findByNickname(userDTO.getNickname()).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(userDTO.getNickname());
        }
        
        if (userDTO.getBirth() != null) {
            user.setBirth(userDTO.getBirth());
        }
        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }
        if (userDTO.getPicture() != null) {
            user.setPicture(userDTO.getPicture()); // setPicture 대신 setProfilePicture 사용 (User 엔티티에 맞게)
        }
        
        User updatedUser = userRepository.save(user);
        
        userSolrService.indexUserDocument(convertToUserDocument(updatedUser));
        
        return updatedUser.toDTO();
    }

    /**
     * 사용자 비밀번호 변경 (현재 비밀번호 확인 후 변경)
     * @param userId 사용자 ID
     * @param oldPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @return 변경 성공 여부
     */
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    /**
     * 사용자 삭제
     * @param userId 삭제할 사용자 ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료: ID {}", userId);
        userSolrService.deleteUserDocument(String.valueOf(userId));
    }
    
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::toDTO);
    }

    /**
     * 사용자 아이디 또는 이메일로 사용자 정보를 조회합니다.
     * @param identifier 아이디 또는 이메일
     * @return 조회된 사용자 정보 (UserDTO), 없으면 null
     * ⭐ UserDTO에 role 정보 포함하도록 수정 (User::toDTO 호출 시 roles 포함) ⭐
     * ⭐ getRolesAsSet() 대신 User::toDTO를 호출하여 UserDTO에 roles가 포함되도록 User::toDTO 메서드를 확인해야 합니다. ⭐
     */
    public UserDTO findUserByUsernameOrEmail(String identifier) {
        Optional<User> userOptional;

        if (identifier.contains("@")) {
            userOptional = userRepository.findByEmail(identifier);
        } else {
            userOptional = userRepository.findByUsername(identifier);
        }

        // ⭐ userEntity.toDTO() 메서드가 UserDTO에 roles를 포함하도록 구현되어 있어야 합니다. ⭐
        return userOptional.map(User::toDTO).orElse(null);
    }

    @Transactional
    public void updateUserPicture(Long userId, String pictureUrl) {
        User user = userRepository.findById(userId)
                                     .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        // user.updatePicture(pictureUrl); // User 엔티티에 updatePicture 메서드가 없다면 setProfilePicture 사용
        user.setPicture(pictureUrl); // 직접 필드 설정
        userRepository.save(user);
        userSolrService.indexUserDocument(convertToUserDocument(user));
    }
    
    @Transactional
    public String createPasswordResetTokenForUser(String email) throws MessagingException {
<<<<<<< HEAD
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다."));

        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
        
        return token;
    }

=======
        // 1. 해당 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다."));

        // 2. 새로운 토큰 값과 만료일 생성
        String newTokenValue = UUID.randomUUID().toString();
        LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(1); // 현재 시간 + 1시간

        // 3. 해당 유저의 기존 비밀번호 재설정 토큰이 있는지 확인
        Optional<PasswordResetToken> existingTokenOptional = passwordResetTokenRepository.findByUser(user);

        PasswordResetToken resetToken;
        if (existingTokenOptional.isPresent()) {
            // 4. 기존 토큰이 있다면, 해당 객체를 가져와 새 토큰 값과 만료일로 '업데이트'
            //    이 방식은 JPA가 트랜잭션 종료 시 변경된 엔티티를 감지하여 UPDATE 쿼리를 실행하도록 합니다.
            resetToken = existingTokenOptional.get();
            resetToken.setToken(newTokenValue);
            resetToken.setExpiryDate(newExpiryDate);
        } else {
            // 5. 기존 토큰이 없다면, 새로운 PasswordResetToken 객체를 빌드하여 생성
            resetToken = PasswordResetToken.builder()
                    .token(newTokenValue)
                    .user(user)
                    .expiryDate(newExpiryDate)
                    .build();
        }
        
        // 6. 업데이트되거나 새로 생성된 토큰을 데이터베이스에 저장
        //    JPA의 save 메서드는 엔티티가 영속 컨텍스트에 있고 ID(Primary Key)가 있으면 UPDATE,
        //    없거나 새로 빌드된 엔티티면 INSERT를 수행합니다.
        passwordResetTokenRepository.save(resetToken);

        // 7. 비밀번호 재설정 이메일 발송
        emailService.sendPasswordResetEmail(user.getEmail(), newTokenValue);
        
        // 8. 생성된 (혹은 업데이트된) 토큰 값 반환
        return newTokenValue;
    }
>>>>>>> main
    @Transactional
    public void handleSuccessfulLogin(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        userOptional.ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0 || user.isAccountLocked()) {
                user.resetFailedLoginAttempts();
                user.unlockAccount();
                userRepository.save(user);
                userSolrService.indexUserDocument(convertToUserDocument(user)); // Solr 문서도 동기화
                log.info("DEBUG: User {} login successful. Account unlocked and failed attempts reset.", username);
            }
        });
    }
    /**
     * JPA User 엔티티를 Solr UserDocument POJO로 변환하는 헬퍼 메서드
     * @param user 변환할 User 엔티티
     * @return 변환된 UserDocument
     */
    private UserDocument convertToUserDocument(User user) {
        // UserDocument 필드에 맞춰 User 엔티티의 데이터를 매핑합니다.
        // UserDocument의 필드 (id, username, nickname, profileDescription, followerCount, roles)에 맞게
        // User 엔티티의 데이터를 가져와 매핑합니다.
        // 만약 User 엔티티에 Solr Document의 특정 필드(예: profileDescription, followerCount)가 없다면
        // 기본값(null, 0 등)을 설정하거나, 해당 필드를 UserDocument에서 제거해야 합니다.

        List<String> rolesAsStrings = user.getRoles().stream()
                                            .map(Role::name) // Enum Role을 String으로 변환
                                            .collect(Collectors.toList());

        return UserDocument.builder()
                .id(String.valueOf(user.getId())) // User 엔티티의 ID (Long)를 String으로 변환
                .username(user.getUsername())
                .nickname(user.getNickname())
                // User 엔티티에 profileDescription 필드가 있다면 매핑, 없으면 null
                .profileDescription(null) // ⭐ User 엔티티에 맞게 수정 필요 ⭐
                // User 엔티티에 followerCount 필드가 있다면 매핑, 없으면 0
                .followerCount(0) // ⭐ User 엔티티에 맞게 수정 필요 ⭐
                .roles(rolesAsStrings)
                .build();
    }
}