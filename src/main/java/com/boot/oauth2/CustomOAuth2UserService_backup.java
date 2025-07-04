package com.boot.oauth2;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;







@Component
public class CustomOAuth2UserService_backup extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, naver, kakao

        Map<String, Object> userInfo;
        System.out.println("🌐 CustomOAuth2UserService - registrationId: " + registrationId);
        switch (registrationId) {
            case "naver":
                userInfo = (Map<String, Object>) attributes.get("response");
                break;
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                userInfo = new HashMap<>();
                userInfo.put("email", kakaoAccount.get("email"));
                userInfo.put("name", profile.get("nickname"));
                System.out.println("✅ Kakao userInfo: " + userInfo);
                break;
            case "google":
            default:
                userInfo = attributes;
                break;
        }
        return oAuth2User;
    }
}
