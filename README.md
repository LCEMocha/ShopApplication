# 🛒 Spring Boot JPA 쇼핑몰 프로젝트
## 💻 기본 기능
* 회원가입, 로그인
* OAuth 소셜회원가입 및 로그인
* 상품 주문 및 취소(실제 결제는 x), 장바구니, 주문내역 조회
* Redis를 이용한 쿠폰 발급
* Kafka를 이용한 챗봇(예정)


## 💻 주요 사용 기술
* Java 20
* Spring
* MySQL
* Redis
* Kafka(예정)

## 💻 ERD
![ERD](https://github.com/LCEMocha/ShopApplication/assets/142338641/74ca2446-da9c-4028-95c8-176674096dc9)


## 🔑 주요 구현
### 1. Redis Pub/Sub을 활용한 쿠폰 발급 비동기 처리
* 올리브영 테크블로그 참고
* <https://oliveyoung.tech/blog/2023-08-07/async-process-of-coupon-issuance-using-redis/>
  ![image](https://github.com/LCEMocha/ShopApplication/assets/142338641/1ad8dea7-37b1-4eea-8be6-8e9376780614)

  1. 쿠폰 발급 Worker가 구동되면 'CouponIssuance' 이라는 Redis Topic에 대한 '일련번호'가 생성됩니다.
  2. 일련번호가 결정되면, CouponIssuance topic에 이 일련번호를 publish 합니다.
  3. 주기적으로 'CouponStore' List의 크기가 0보다 큰지 확인하고, 0보다 크면 List에서 데이터를 Pop 하여 쿠폰을 발급하는 프로세스가 실행됩니다
     (모종의 이유로, 요청을 받고도 미발급된 쿠폰이 있을 것을 방지).

  위의 1~3번 과정은 기본 구성요소와 초기 설정이므로, 쿠폰 발급을 위해 기본적으로 미리 실행되어 있어야 합니다.
  아래 4~8번 과정은 실제 사용자가 쿠폰 발급을 요청했을 때 어떤 흐름으로 동작하는지 설명합니다.
  
  4. 고객이 쿠폰 발급을 요청합니다.
  5. CouponController는 이 요청을 받고 Worker들이 가지고 있는 일련번호 중 1개를 골라서 CouponIssuance topic에 publish 합니다.
     CouponMessageListener는 이 채널을 subscribe하고 있으므로, 이를 감지합니다.
  6. 쿠폰을 요청한 사용자정보(email)와 쿠폰 일련번호를 CouponStore에 Rpush합니다. 이를 통해 발급 대기 중인 쿠폰 요청이 순차적으로 관리됩니다.
  7. 5번의 과정으로 CouponMessageListener는 handleCouponRequest를 호출하여 실제 쿠폰 처리 로직을 수행합니다.
     checkAndIssueCoupon 메서드는 CouponStore 리스트에서 해당 일련번호를 가진 쿠폰 발급 요청 데이터를 찾습니다(Lpop).
  8. 7번에서 가져온 쿠폰데이터를 조회하여 해당 쿠폰 재고수량을 확인하고, 재고가 있으면 1개를 차감한 뒤 Main DB(MySQL)에 쿠폰정보를 INSERT하여 발급 처리를 완료합니다.
 
* 분산락을 사용하여 비동기 처리과정에서 DB수정 시 충돌이 일어나지 않도록 방지하였습니다.


### 2. 대기열 구현
* 우아한Tech 유튜브 참고
* https://www.youtube.com/watch?v=MTSn93rNPPE
  
  ![image](https://github.com/LCEMocha/ShopApplication/assets/142338641/c75313b7-c19a-4530-927e-994c39ea4e42)
* 1000개의 스레드에서 동시에 쿠폰 발급을 요청하는 테스트코드를 통과하였습니다.


### 3. Kafka를 이용한 상담원 채팅 기능(예정)
*

## 🔧 한계
* 아직 미완성 프로젝트로, 향후 Kafka를 이용한 상담원 채팅 기능을 만들어보고 EC2에 배포하고자 합니다. 
* 개인적으로 로컬 환경에서만 진행한 프로젝트이기 때문에 배포와 협업을 해보지 못한 점이 아쉽습니다. 향후 GitHub Action툴을 이용한 CI/CD에 대해 공부해보고자 합니다.
* 테스트코드 작성에 소홀했다고 생각됩니다(특히 단위테스트).
