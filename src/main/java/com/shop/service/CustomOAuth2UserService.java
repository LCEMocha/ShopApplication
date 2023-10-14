package com.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 사용하여 사용자의 정보를 가져옴
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // 로그인한 사용자의 정보를 바탕으로 DB에 저장 또는 업데이트
        memberService.saveOAuth2Member(userRequest, oAuth2User);

        // 최종적으로 OAuth2User 객체를 반환
        return oAuth2User;
    }
}

