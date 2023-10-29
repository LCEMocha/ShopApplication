package com.shop.service;

import com.shop.constant.Role;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.oauth2.core.user.OAuth2User;

@Service
@Transactional  //비즈니스 로직을 담당하는 서비스계층 클래스에 @Transactional 어노테이션을 선언
                // 로직을 처리하다 에러가 발생했다면, 변경된 데이터를 로직을 수행하기 이전 상태로 콜백시켜준다.
@RequiredArgsConstructor
//빈을 주입하는 방법으로는 @Autowired 어노테이션을 사용하거나, 필드 주입(@Setter), 생성자 주입을 이용하는 방법이 있다.
// 위 어노테이션은 final이나 @NonNull이 붙은 필드에 생성자를 생성해준다.
// 빈에 생성자가 1개이고 생성자의 파라미터 타입이 빈으로 등록이 가능하다면 @Autowired 없이 의존성 주입이 가능하다.
public class MemberService implements UserDetailsService{

    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member){  //이미 가입된 회원의 경우 Exception 발생
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    //UserDetailsService 인터페이스의 loadUserByUsername() 메소드를 오버라이딩한다. 로그인할 유저의 email을 파라미터로 전달받는다.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }

        return User.builder() //UserDetail을 구현하고 있는 User 객체 반환
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    public Member saveOAuth2Member(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        String provider = userRequest.getClientRegistration().getRegistrationId(); // OAuth2 제공자의 이름 (예: google)
        String providerId = oAuth2User.getName(); // 제공자의 사용자 ID
        String email = oAuth2User.getAttribute("email"); // 이메일
        String name = oAuth2User.getAttribute("name"); // 이름

        //System.out.println("Searching for email: " + email);

        Member member = memberRepository.findByProviderAndProviderId(provider, providerId);

        if(member == null) {
            member = new Member();
            member.setProvider(provider);
            member.setProviderId(providerId);
            member.setEmail(email);
            member.setName(name);
            member.setRole(Role.USER); // 기본 역할 설정

            return memberRepository.save(member);
        }

        //System.out.println("Searching for email: " + email);
        return member;
    }

}
