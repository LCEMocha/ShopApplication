package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter @Setter
public class OrderItem extends BaseEntity{

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 하나의 상품은 여러 주문 상품으로 들어갈 수 있으므로 주문상품 기준으로 다대일 단방향 매핑
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)  // 한 번의 주문에 여러 개의 상품을 주문할 수 있으므로 다대일 단방향 매핑
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문가격

    private int count; //수량

    public static OrderItem createOrderItem(Item item, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);  // 주문할 상품과,
        orderItem.setCount(count);  // 주문할 수량 세팅
        orderItem.setOrderPrice(item.getPrice());  // 현재시간 기준으로 상품가격을 주문가격으로 세팅(상품가격이 시간에따라 달라질수 있으므로)
        item.removeStock(count);  // 주문수량만큼 상품의 재고수량 감소
        return orderItem;
    }

    public int getTotalPrice(){  // 주문가격*주문수량으로 해당 상품을 주문한 총 가격 계산 메소드
        return orderPrice*count;
    }

    public void cancel() {
        this.getItem().addStock(count);
    }

}
