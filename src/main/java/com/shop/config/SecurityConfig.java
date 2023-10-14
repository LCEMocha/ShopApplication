package com.shop.config;

import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    @Autowired
    MemberService memberService;

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http.formLogin()
                .loginPage("/members/login")
                .defaultSuccessUrl("http://localhost:8080/")
                .usernameParameter("email")
                .failureUrl("/members/login/error")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/")
        ;

        http.oauth2Login()
                .loginPage("/oauth2/authorization/google") //Google 로그인 페이지로 리다이렉트
                .defaultSuccessUrl("http://localhost:8080/")
                .failureUrl("/loginFailure")
                .userInfoEndpoint()
        ;

        http.authorizeHttpRequests((authorize) -> authorize
        //permitAll()을 통해 모든 사용자가 인증(로그인) 없이 해당 경로에 접근할 수 있도록 설정한다.
        //admin으로 시작하는 경로는 해당 계정이 ADMIN Role일 때만 접근 가능하도록 설정한다.
                .requestMatchers(antMatcher("/css/**")).permitAll()
                .requestMatchers(antMatcher("/js/**")).permitAll()
                .requestMatchers(antMatcher("/img/**")).permitAll()
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/members/**")).permitAll()
                .requestMatchers(antMatcher("/item/**")).permitAll()
                .requestMatchers(antMatcher("/images/**")).permitAll()
                .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                .anyRequest().authenticated() //설정한 경로를 제외한 나머지 경로들은 모두 인증을 요구하도록 설정한다.
        )
        ;

        http.exceptionHandling()
                //인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러를 등록한다.
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}