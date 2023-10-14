package com.shop.controller;

import com.shop.config.SecurityUtils;
import com.shop.dto.CartItemDto;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import com.shop.dto.CartDetailDto;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.shop.dto.CartOrderDto;


@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal){

        // 장바구니에 담을 상품 정보를 받는 cartItemDto 객체에 데이터 바인딩 시 에러가 있는지 검사
        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        // 현재 로그인한 회원의 이메일정보를 변수에 저장
        String email = SecurityUtils.getEmailFromPrincipal(principal);
        Long cartItemId;

        try {
            // 화면으로부터 넘어온 장바구니에 담을 상품 정보와 회원의 이메일 정보를 이용하여 장바구니에 상품을 담는 로직 호출
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // 결과값으로 생성된 장바구니 상품 아이디와 요청이 성공하였다는 HTTP 응답 상태 코드 반환
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model){

        String emailOrId = SecurityUtils.getEmailFromPrincipal(principal);

        // 현재 사용자 이메일 정보를 이용하여 장바구니에 담겨있는 상품 정보 조회
        List<CartDetailDto> cartDetailList = cartService.getCartList(emailOrId);
        // 조회한 장바구니 상품 정보를 뷰로 전달
        model.addAttribute("cartItems", cartDetailList);
        return "cart/cartList";
    }

    // HTTP 메소드에서 PATCH는 요청된 자원의 일부를 업데이트할 때 PATCH를 사용한다.
    // 장바구니 상품의 수량만 업데이트하기 때문에 @PatchMapping을 사용한다.
    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId, int count, Principal principal){

        String emailOrId = SecurityUtils.getEmailFromPrincipal(principal);

        if(count <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        } else if(!cartService.validateCartItem(cartItemId, emailOrId)){  // 수정 권한 체크
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);  // 장바구니 상품의 개수 업데이트
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    // 장바구니 상품을 삭제하기 때문에 @DeleteMapping을 사용한다.
    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal){

        String emailOrId = SecurityUtils.getEmailFromPrincipal(principal);

        if(!cartService.validateCartItem(cartItemId, emailOrId)){  // 수정 권한 체크
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);  // 해당 장바구니 상품 삭제

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal){

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        // 주문할 상품을 선택하지 않았는지 체크
        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }

        // 주문권한 체크
        for (CartOrderDto cartOrder : cartOrderDtoList) {

            String emailOrId = SecurityUtils.getEmailFromPrincipal(principal);

            if(!cartService.validateCartItem(cartOrder.getCartItemId(), emailOrId)){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        String emailOrId = SecurityUtils.getEmailFromPrincipal(principal);

        // 주문 로직 호출 결과 생성된 주문 번호 반환
        Long orderId = cartService.orderCartItem(cartOrderDtoList, emailOrId);
        // 생성된 주문 번호와 요청이 성공했다는 HTTP 응답 상태 코드 반환
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}