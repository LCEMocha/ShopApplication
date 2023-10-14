package com.shop.repository;

import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>{
    Member findByEmail(String email); //중복여부 검사

    Member findByProviderAndProviderId(String provider, String providerId);

}
