package com.shop.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    public ChatController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/{id}")
    public String chattingRoom(@PathVariable String id, HttpSession session, Model model, Principal principal){
        if(id.equals("user")){

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            String userName = oauthToken.getPrincipal().getAttribute("name");
            model.addAttribute("name", userName);
        }else if(id.equals("master")){
            model.addAttribute("name", "상담원");
        }else if(id.equals("loose")){
            model.addAttribute("name", "loose");
        }
        return "chat/chattingRoom2";
    }
}