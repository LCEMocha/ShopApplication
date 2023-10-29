package com.shop.config;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;
import java.util.Map;

public class SecurityUtils {

    public static String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();

            // 제공자 정보 가져오기
            String authorizedClientRegistrationId = oauth2Token.getAuthorizedClientRegistrationId();

            if ("kakao".equals(authorizedClientRegistrationId)) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    String email = (String) kakaoAccount.get("email");
                    if (email != null) {
                        return email;
                    }
                }
            } else if ("google".equals(authorizedClientRegistrationId)) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    return email;
                }
            }

            throw new RuntimeException("Email not found in OAuth2 token");
        } else {
            return principal.getName(); // 일반 회원의 경우 이메일이 principal.getName()에 있을 것으로 가정
        }
    }
}