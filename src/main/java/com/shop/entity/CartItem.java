package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter @Setter
@Table(name="cart_item")
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 장바구니에는 여러 상품을 담을 수 있으므로 다대일 관계로 매핑
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")  // 장바구니에 담을 상품의 정보를 알아야 하므로 상품 엔티티를 매핑
    private Item item;

    private int count;  // 같은 상품을 장바구니에 몇 개 담을지를 지정


    public static CartItem createCartItem(Cart cart, Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(count);
        return cartItem;
    }

    // 이미 담겨 있는 상품을 추가로 장바구니에 담을 때 기존 수량에 현재 담을 수량을 더해주는 메서드
    public void addCount(int count){
        this.count += count;
    }

    public void updateCount(int count){
        this.count = count;
    }

}
