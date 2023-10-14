package com.shop.entity;

import com.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 한 명의 회원은 여러번 주문을 할 수 있으므로 주문 엔티티 기준 다대일 단방향 매핑이다
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderDate; //주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; //주문상태

    // 주문 상품 엔티티와 일대다 매핑을 한다. 외래키(order_id)가 order_item 테이블에 있으므로 연관관계의 주인은 OrerItem 엔티티이다.
    // Order 엔티티가 주인이 아니므로 mappedBy='order'로 연관관계의 주인을 설정한다. OrderItem에 있는 Order에 의해 관리된다는 의미이다.
    // 즉 연관관계의 주인의 필드인 order를 mappedBy의 값으로 세팅하는 것이다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) // 부모 엔티티의 영속성 상태변화를 자식에게 모두 전이하는 CascadeType.ALL
    private List<OrderItem> orderItems = new ArrayList<>();  // 하나의 주문이 여러개의 상품을 가지므로 List 자료형 사용


    // orderItems에는 주문 상품 정보들을 담아준다. orderItem 객체를 order 객체의 orderItems에 추가한다.
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        // Orer와 OrderItem 엔티티가 양방향 참조 관계이므로, orderItem 객체에도 order 객체를 세팅한다.
        orderItem.setOrder(this);
    }

    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member);  // 상품을 주문한 회원의 정보 세팅

        // 상품페이지에서는 1개의 상품을 주문하지만, 장바구니 페이지에서는 여러 개의 상품을 주문할 수 있다.
        // 따라서 여러 개의 주문 상품을 담을 수 있도록 리스트형태로 파라미터 값을 받으며 주문 객체에 orderItem 객체를 추가한다.
        for(OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }

        order.setOrderStatus(OrderStatus.ORDER);  // 주문 상태를 order로 세팅
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public int getTotalPrice() {  // 총 주문금액 메소드
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

}
