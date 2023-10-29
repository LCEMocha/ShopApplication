package com.shop.service;

import java.util.Collections;
import java.util.Map;

import com.shop.constant.Role;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final HttpSession httpSession;
    private final MemberRepository memberRepository;

    //OAuth 2.0 제공자로부터 사용자 정보를 로드하고 로그인 세션 저장 수행
    //Spring Security에서 이 메서드의 반환값(OAuth2User 객체)를 사용하여 인증된 사용자의 정보를 관리하고, 보안 관련 작업을 수행
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("attributes :: " + attributes);

        httpSession.setAttribute("login_info", attributes);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(), "id");
    }

    // KakaoOAuth2Member의 회원정보 조회 및 가입
    public Member saveKakaoOAuth2Member(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getName();

        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
        String email = null;
        String name = null;
        if (kakaoAccount != null) {
            email = (String) kakaoAccount.get("email");

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                name = (String) profile.get("nickname");  // 이름
            }
        }

        log.info("Searching for email, name: " + email + ", " + name);

        Member member = memberRepository.findByProviderAndProviderId(provider, providerId);

        if (member == null) {
            member = new Member();
            member.setProvider(provider);
            member.setProviderId(providerId);
            member.setEmail(email);
            member.setName(name);
            member.setRole(Role.USER); // 기본 역할 설정

            return memberRepository.save(member);
        }

        return member;
    }

}
