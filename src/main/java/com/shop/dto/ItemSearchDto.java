package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemSearchDto {

    private String searchDateType;  // 현재시간과 상품 등록일을 비교해서 상품 데이터를 조회한다

    private ItemSellStatus searchSellStatus;  // 상품의 판매상태 기준

    private String searchBy;  // 상품을 조회할 때 어떤 유형으로(상품명, 등록자id) 조회할지 선택

    private String searchQuery = "";  // 조회할 검색어를 저장할 변수

}