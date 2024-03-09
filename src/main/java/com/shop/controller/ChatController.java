package com.shop.controller;

import java.security.Principal;
import java.util.Map;

import com.shop.entity.Member;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shop.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    MemberRepository memberRepository;

    ServletRequest request;

    public ChatController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/{id}")
    public String chattingRoom(@PathVariable String id, HttpSession session, Model model, Principal principal, HttpServletRequest request) {
        if ("user".equals(id)) {
            if (principal instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
                OAuth2User oauthUser = oauthToken.getPrincipal();
                String authorizedClientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
                String userName = null;

                if ("kakao".equals(authorizedClientRegistrationId)) {
                    Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
                    if (kakaoAccount != null) {
                        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                        if (profile != null) {
                            userName = (String) profile.get("nickname"); // 이름 가져오기
                        }
                    }
                    if (userName == null) { // 이름이 없는 경우 예외 처리
                        throw new RuntimeException("회원명을 찾을 수 없습니다.");
                    }
                } else if ("google".equals(authorizedClientRegistrationId)) {
                    userName = oauthUser.getAttribute("name");
                    if (userName == null) {
                        throw new RuntimeException("회원명을 찾을 수 없습니다.");
                    }
                }

                model.addAttribute("name", userName); // 모델에 이름 추가
            } else {
                // 일반로그인 회원의 경우
                String email = principal.getName();
                Member userName = memberRepository.findByEmail(email);
                model.addAttribute("name", userName.getName());
            }
        } else if (id.equals("master")) {
            model.addAttribute("name", "상담원");
        } else if ("guest".equals(id)) {
            // 클라이언트의 IP 주소 얻기
            String clientIp = request.getRemoteAddr();
            model.addAttribute("name", "guest - " + clientIp);
        }
        return "chattingRoom2";
    }
}