package com.boot.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.boot.domain.User; // User 엔티티 임포트
import com.boot.repository.UserRepository; // UserRepository 임포트

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("🔍 [CustomUserDetailsService] 사용자 '{}' 로드 시작", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("❌ [CustomUserDetailsService] 사용자를 찾을 수 없습니다: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        logger.debug("✅ [CustomUserDetailsService] 사용자 '{}' 데이터베이스에서 조회 완료. ID: {}", username, user.getId());

        // ⭐ 추가: 계정 잠금 상태 확인
        if (user.isAccountLocked()) {
            logger.warn("⚠️ [CustomUserDetailsService] 계정 잠금: 사용자 '{}' 계정이 잠겨있습니다.", username);
            throw new LockedException("계정이 잠겨있습니다. 관리자에게 문의하세요.");
        }

        // 이 시점에서 User 엔티티의 roles 필드는 JPA에 의해 EAGER 로딩되었거나,
        // getAuthorities() 호출 시점에 LAZY 로딩될 것입니다.
        logger.info("✅ [CustomUserDetailsService] 사용자 '{}'에 대한 UserDetails 로드 완료. (권한은 User 엔티티의 getAuthorities()에서 제공)", username);
        return user; // 변경: User 엔티티를 직접 반환
    }
}
//package com.boot.security;
//
//import java.util.Collection; // Collection 임포트 추가
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.slf4j.Logger; // Logger 임포트 추가
//import org.slf4j.LoggerFactory; // LoggerFactory 임포트 추가
//import org.springframework.security.authentication.LockedException;
//import org.springframework.security.core.GrantedAuthority; // GrantedAuthority 임포트 추가
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import com.boot.domain.User; // User 엔티티 임포트
//import com.boot.repository.UserRepository; // UserRepository 임포트
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class); // 로거 인스턴스 생성
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        logger.debug("🔍 [CustomUserDetailsService] 사용자 '{}' 로드 시작", username);
//
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> {
//                    logger.warn("❌ [CustomUserDetailsService] 사용자를 찾을 수 없습니다: {}", username);
//                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
//                });
//
//        logger.debug("✅ [CustomUserDetailsService] 사용자 '{}' 데이터베이스에서 조회 완료. ID: {}", username, user.getId());
//
//        // ⭐ 추가: 계정 잠금 상태 확인
//        if (user.isAccountLocked()) {
//            logger.warn("⚠️ [CustomUserDetailsService] 계정 잠금: 사용자 '{}' 계정이 잠겨있습니다.", username);
//            throw new LockedException("계정이 잠겨있습니다. 관리자에게 문의하세요.");
//        }
//
//        // ⭐ 핵심 로그: User 엔티티의 역할을 확인하고 GrantedAuthority로 변환하는 과정
//        Set<SimpleGrantedAuthority> grantedAuthorities = user.getRoles().stream()
//                .map(role -> {
//                    // role.name()은 Enum의 문자열 이름을 가져옵니다.
//                    // Spring Security는 'ROLE_' 접두사를 기대하므로, User 엔티티의 Role Enum이 'ADMIN'이면 'ROLE_ADMIN'으로 변환해야 합니다.
//                    String authorityName = "ROLE_" + role.name(); // Role Enum의 이름에 ROLE_ 접두사 추가
//                    logger.debug("➡️ [CustomUserDetailsService] 사용자 '{}' 역할 변환: {} -> {}", username, role.name(), authorityName);
//                    return new SimpleGrantedAuthority(authorityName);
//                })
//                .collect(Collectors.toSet());
//
//        logger.info("✅ [CustomUserDetailsService] 사용자 '{}'에게 부여된 최종 권한: {}", username,
//                grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
//
//        // CustomUserDetails 반환
//        return new CustomUserDetails(user); // 변경: grantedAuthorities를 CustomUserDetails로 전달
//    }
//}