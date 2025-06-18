package com.boot.repository;

import com.boot.domain.User; // User 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 임포트
import org.springframework.stereotype.Repository; // Repository 어노테이션 임포트
import java.util.Optional; // Optional 타입 임포트 (데이터가 없을 수도 있기 때문에 사용)

@Repository // 이 인터페이스가 Spring Bean으로 등록되도록 지정
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository를 상속받으면 기본적인 CRUD(Create, Read, Update, Delete) 메서드가 자동으로 제공됩니다.
    // 예를 들어, save(), findById(), findAll(), delete() 등이 있습니다.

    /**
     * 사용자 이름(username)으로 User 엔티티를 조회합니다.
     * 일반 로그인 시 사용자 존재 여부 확인 및 정보 로딩에 사용됩니다.
     * @param username 조회할 사용자 이름
     * @return User 엔티티를 포함하는 Optional 객체
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일(email)로 User 엔티티를 조회합니다.
     * 회원 가입 시 이메일 중복 확인, 이메일 기반 로그인 등에 사용될 수 있습니다.
     * @param email 조회할 이메일 주소
     * @return User 엔티티를 포함하는 Optional 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자 이름(username) 또는 이메일(email) 중 하나로 User 엔티티를 조회합니다.
     * 사용자가 로그인 시 ID/이메일 중 아무것이나 입력할 수 있도록 할 때 유용합니다.
     * @param username 조회할 사용자 이름
     * @param email 조회할 이메일 주소
     * @return User 엔티티를 포함하는 Optional 객체
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * OAuth2 제공자(provider)와 해당 제공자 내에서의 고유 ID(providerId)로 User 엔티티를 조회합니다.
     * 소셜 로그인 시 이미 가입된 사용자인지 확인하는 데 사용됩니다.
     * @param provider OAuth2 제공자 (예: "google", "naver", "kakao")
     * @param providerId 해당 제공자 내에서의 사용자 고유 ID
     * @return User 엔티티를 포함하는 Optional 객체
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    /**
     * 닉네임(nickname)으로 User 엔티티를 조회합니다.
     * 닉네임 중복 확인 등에 사용될 수 있습니다.
     * @param nickname 조회할 닉네임
     * @return User 엔티티를 포함하는 Optional 객체
     */
    Optional<User> findByNickname(String nickname);
}