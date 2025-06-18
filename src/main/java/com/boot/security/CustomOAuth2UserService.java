package com.boot.security;

import com.boot.domain.User; // User 엔티티 import
import com.boot.oauth2.OAuth2UserInfo;
import com.boot.oauth2.OAuth2UserInfoFactory;
import com.boot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                                            .getUserInfoEndpoint().getUserNameAttributeName();

        logger.info("🌐 [CustomOAuth2UserService] OAuth2 Provider ID: {}", registrationId);
        logger.info("🌐 [CustomOAuth2UserService] User Info Endpoint Attribute Name: {}", userNameAttributeName);

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());

        String email = oAuth2UserInfo.getEmail();
        String nickname = oAuth2UserInfo.getNickname();
        String providerId = oAuth2UserInfo.getProviderId();
        String profilePictureUrl = oAuth2UserInfo.getImageUrl();

        logger.info("🌐 [CustomOAuth2UserService] Extracted: Email={}, Nickname={}, ProviderId={}", email, nickname, providerId);

        // ⭐핵심 수정: userService.processOAuthUser 메서드의 반환 타입이 User 엔티티여야 합니다.⭐
        // 아래 코드가 오류 없이 실행되려면, UserService의 processOAuthUser 메서드가 User 엔티티를 반환하도록 수정해야 합니다.
        User userEntity = userService.processOAuthUser(registrationId, providerId, email, nickname, profilePictureUrl);
        logger.info("✅ [CustomOAuth2UserService] User entity processed: ID={}, Username(Email)={}", userEntity.getId(), userEntity.getUsername());

        return new CustomOAuth2User(userEntity);
    }
}