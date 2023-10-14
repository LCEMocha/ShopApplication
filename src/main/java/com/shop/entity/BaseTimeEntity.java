package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@EntityListeners(value = {AuditingEntityListener.class})  //Auditing을 적용하기 위해 필요한 어노테이션
@MappedSuperclass  // 공통매핑 정보가 필요할 때 사용하는 어노테이션으로, 부모 클래스를 자식 클래스에 매핑 정보만 제공
@Getter @Setter
public abstract class BaseTimeEntity {

    @CreatedDate  // 엔티티가 생성되어 저장될 때 시간을 자동으로 저장
    @Column(updatable = false)
    private LocalDateTime regTime;

    @LastModifiedDate  // 엔티티 값 변경시간 저장
    private LocalDateTime updateTime;

}