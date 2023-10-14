package com.shop.repository;

import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDateTime;
import java.util.List;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.shop.entity.QItem;
//import javax.persistence.PersistanceContext;
//import javax.persistence.EntityManger;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;


@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")

class ItemRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ItemRepository itemRepository;

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest(){
        Item item = new Item();
        item.setItemNm("테스트상품2");
        item.setPrice(20000);
        item.setItemDetail("테스트상품 상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        Item savedItem = itemRepository.save(item);
        System.out.println(savedItem.toString());
    }

    public void createItemList(){
        for(int i=1; i<=10; i++){
            Item item = new Item();
            item.setItemNm("테스트상품" + i);
            item.setPrice(10000+i);
            item.setItemDetail("테스트상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            Item savedItem = itemRepository.save(item);
        }
    }

    @Test
    @DisplayName("상품명 조회 테스트")
    public void findByItemNmTest(){
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemNm("테스트상품1");
        for(Item item : itemList){
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailTest(){
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetail("테스트상품 상세 설명");
        for (Item item : itemList){
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("nativeQuery 속성을 이용한 상품 조회 테스트")
    public void findByItemDetailByNative(){
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetailByNative("테스트상품 상세 설명");
        for (Item item : itemList){
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("Querydsl 조회 테스트1")
    public void queryDslTest(){
        this.createItemList();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QItem qItem = QItem.item;
        JPAQuery<Item> query = queryFactory.selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
                .where(qItem.itemDetail.like("%"+"테스트상품 상세 설명"+"%"))
                .orderBy(qItem.price.desc());
        List<Item> itemList = query.fetch();

        for (Item item : itemList){
            System.out.println(item.toString());
        }
    }

    public void createItemList2(){  //상품데이터를 만드는 새로운 메소드
        for(int i=1; i<=5; i++){
            Item item = new Item();
            item.setItemNm("테스트상품" + i);
            item.setPrice(10000+i);
            item.setItemDetail("테스트상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemRepository.save(item);
        }

        for(int i=6; i<=10; i++){
            Item item = new Item();
            item.setItemNm("테스트상품" + i);
            item.setPrice(10000+i);
            item.setItemDetail("테스트상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SOLD_OUT);
            item.setStockNumber(0);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemRepository.save(item);
        }
    }
    @Test
    @DisplayName("상품 Querydsl 조회 테스트 2")
    public void queryDslTest2(){

        this.createItemList2();

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        //BooleanBuilder는 쿼리에 들어갈 조건을 만들어주는 빌더이다. Predicate를 구현하고 있으며, 메소드 체인 형식으로 사용할 수 있다.
        QItem item = QItem.item;
        String itemDetail = "테스트상품 상세 설명";
        int price = 10003;
        String itemSellStat = "SELL";

        booleanBuilder.and(item.itemDetail.like("%"+itemDetail+"%"));
        //필요한 상품을 조회하는데 필요한 and 조건 추가. 아래 소스에서 상품의 판매상태가 SELL일때만 booleanBuilder에 판매상태 조건을 동적으로 추가하고 있다.
        booleanBuilder.and(item.price.gt(price));

        if (StringUtils.equals(itemSellStat, ItemSellStatus.SELL)){
            booleanBuilder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));
        }

        Pageable pageable = PageRequest.of(0,5);
        //데이터를 페이징해 조회하도록 PageRequest.of() 메소드를 이용하여 Pageable 객체를 생성한다. 첫번째 인자는 조회할 페이지 번호, 두번째 인자는 한 페이지당 조회할 데이터의 개수이다.
        Page<Item> itemPagingResult = itemRepository.findAll(booleanBuilder, pageable); //findAll 메소드를 이용해 조건에 맞는 데이터를 Page객체로 받아온다.
        System.out.println("total elements : " + itemPagingResult. getTotalElements());

        List<Item> resultItemList = itemPagingResult.getContent();
        for(Item resultItem: resultItemList){
            System.out.println(resultItem.toString());
        }
    }

}