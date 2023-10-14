package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.*;


@Entity
@Table(name="member")
@Getter @Setter
@ToString
public class Member extends BaseEntity{
    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    //회원은 이메일을 통해 유일하게 구분해야하므로, DB에 중복값이 없도록 uniqe 속성을 지정한다.
    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    @Column
    private String provider;

    @Column
    private String providerId;


    //자바의 enum 타입을 엔티티의 속성으로 지정할 수 있다.
    //Enum을 사용할 때 기본적으로 순서가 저장되는데, 이 순서가 바뀔 경우 문제가 생길 수 있으므로 String으로 저장하기를 권장.
    @Enumerated(EnumType.STRING)
    private Role role;

    //Member 엔티티를 생성하는 메소드이다.
    //Member 엔티티에 회원을 생성하는 메소드를 만들어서 관리한다면 코드가 변경되더라도 한 군데만 수정하면 된다.
    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword()); //비밀번호 암호화
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }
}
