package com.shop.config;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;

public class SecurityUtils {

    public static String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");

            if (email == null) {
                throw new RuntimeException("Email not found in OAuth2 token");
            }
            return email;
        } else {
            return principal.getName(); // 일반 회원의 경우 이메일이 principal.getName()에 있을 것으로 가정
        }
    }
}