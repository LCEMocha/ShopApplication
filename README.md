# 🛒 Spring Boot JPA 쇼핑몰 프로젝트
## 💻 기본 기능
* 회원가입, 로그인
* OAuth 소셜회원가입 및 로그인
* 상품 주문 및 취소(실제 결제는 x), 장바구니, 주문내역 조회
* Redis를 이용한 쿠폰 발급
* Kafka를 이용한 챗봇
* Docker 컨테이너를 이용하여 EC2 인스턴스에 배포


## 💻 주요 사용 기술
* Java 20
* Spring
* MySQL
* Redis
* Kafka
* Docker
* EC2

## 💻 ERD
![ERD](https://github.com/LCEMocha/ShopApplication/assets/142338641/74ca2446-da9c-4028-95c8-176674096dc9)


## 🔑 주요 구현
### 1. Redis Pub/Sub을 활용한 쿠폰 발급 비동기 처리
* 올리브영 테크블로그 참고 : <https://oliveyoung.tech/blog/2023-08-07/async-process-of-coupon-issuance-using-redis/>
* 동시다발적으로 발생하는 실시간 쿠폰발급 요청을 빠르고 안정적으로 처리하도록 구현
* 요청을 비동기적으로 처리하여 빠르고, Redis의 '인-메모리 데이터 스토어'라는 특성을 이용하여 처리 속도 향상과 동시에 DB에 부하를 줄임
* pub/sub의 단순한 구조상 데이터의 영속성과 배달을 보장하진 못하므로, 쿠폰 발급 데이터의 유실을 방지하기 위해 Redis에서 제공하는 List 자료구조를 추가로 활용하여 비교적 안정적 
  ![image](https://github.com/LCEMocha/ShopApplication/assets/142338641/1ad8dea7-37b1-4eea-8be6-8e9376780614)
  
  1. 쿠폰 발급 Worker가 구동되면 '쿠폰발급(CouponIssuance)' 이라는 Redis Topic에 대한 '일련번호'가 생성됩니다.
  2. 일련번호가 결정되면, CouponIssuance topic에 이 일련번호를 publish 합니다.
  3. 주기적으로 '쿠폰 발급 저장소(CouponStore)' List의 크기가 0보다 큰지 확인하고, 0보다 크면 List에서 데이터를 Pop 하여 쿠폰을 발급하는 프로세스가 실행됩니다
     (쿠폰 발급 데이터 유실 방지).

  위의 1-3번 과정은 기본 구성요소와 초기 설정이므로, 쿠폰 발급을 위해 기본적으로 미리 실행되어 있어야 합니다.
  아래 4-8번 과정은 실제 사용자가 쿠폰 발급을 요청했을 때 어떤 흐름으로 동작하는지 설명합니다.
  
  4. 고객이 쿠폰 발급을 요청합니다.
  5. CouponController는 요청을 받고 Worker들이 가지고 있는 일련번호 중 1개를 골라서 CouponIssuance topic에 publish 합니다.
     CouponMessageListener는 해당 채널을 subscribe하고 있으므로, 이를 감지합니다.
  6. 쿠폰을 요청한 사용자정보(email)와 쿠폰 일련번호를 쿠폰 발급 저장소에 Rpush합니다. 이를 통해 발급 대기 중인 쿠폰 요청이 순차적으로 관리됩니다.
  7. 5번의 과정으로 CouponMessageListener는 쿠폰발급 처리 로직이 구현된 메서드를 호출합니다.
     호출된 쿠폰발급 처리 메서드(checkAndIssueCoupon)는 쿠폰 저장소 리스트에서 해당 일련번호를 가진 쿠폰 발급 요청 데이터를 찾습니다(Lpop).
  8. 7번에서 가져온 쿠폰데이터를 조회하여 해당 쿠폰 재고수량을 확인하고, 재고가 있으면 1개를 차감한 뒤 Main DB(MySQL)에 쿠폰정보를 INSERT하여 발급 처리를 완료합니다.
 
* 분산락을 사용하여 비동기 처리과정에서 DB수정 시 충돌이 일어나지 않도록 방지하였습니다.
* [CouponWorker 코드](https://github.com/LCEMocha/ShopApplication/blob/master/src/main/java/com/shop/service/CouponWorker.java)
* [Coupon 발행 Controller 코드](https://github.com/LCEMocha/ShopApplication/blob/master/src/main/java/com/shop/controller/CouponController.java)
* [redis 설정](https://github.com/LCEMocha/ShopApplication/blob/master/src/main/java/com/shop/config/RedisConfig.java)
* [분산락 설정](https://github.com/LCEMocha/ShopApplication/blob/master/src/main/java/com/shop/config/DistributedLock.java)

### 2. Redis Sorted Set 이용한 대기열 구현
* 우아한Tech 유튜브 참고 : https://www.youtube.com/watch?v=MTSn93rNPPE
* 기준 시간(쿠폰이 출시된 시간)으로부터 고객의 요청까지 걸린 시간을 점수화(ZADD)하여 선착순 순서를 부여(ZRANK)
* 5초에 한번씩, 대기열의 상위 200명을 참가열(쿠폰발급 서비스로직 시작)로 전송
  
  <img src="https://github.com/LCEMocha/ShopApplication/assets/142338641/c75313b7-c19a-4530-927e-994c39ea4e42" width="750" height="400"/>
* 1000개의 스레드에서 동시에 쿠폰 발급을 요청하는 테스트코드를 통과하였습니다.
* [대기열 구현 코드](https://github.com/LCEMocha/ShopApplication/blob/master/src/main/java/com/shop/controller/IssuanceQueue.java)


### 3. Kafka를 이용한 상담원 채팅 기능
* WebSocket으로 실시간 채팅 애플리케이션을 구현하는 데 Kafka 메시지 큐를 사용함으로써 실시간 메시지 전송의 신뢰성과 확장성 ↑
* Kafka를 사용한 이유:
  1. WebSocket 서버 앞에 분산 메시지 브로커 시스템인 kafka를 두어 부하를 분산시킴으로써 대규모의 데이터 스트림을 효율적으로 처리할 수 있습니다.
  2. Kafka는 메시지를 디스크에 저장하고 복제하여 고가용성을 보장합니다. 이로 인해 시스템 장애가 발생해도 메시지 손실 없이 데이터를 보존하고 복구할 수 있습니다.
  3. Kafka에게 채팅방과 메시지 라우팅의 복잡한 상태 관리를 위임시키며, 좀 더 간편하고 효율적인 채팅시스템을 구축할 수 있습니다.
<img src="https://github.com/user-attachments/assets/6bd97834-f3bd-426d-ac00-cc3c8462a11d" width="450" height="600"/>




## 🔧 한계
* 개인적으로 진행한 프로젝트이기 때문에 협업을 해보지 못한 점이 아쉽습니다.
* 단위테스트코드 작성을 다양화했으면 좋을 것 같다는 아쉬움이 있습니다.
* 프론트 구현은 쇼핑몰의 큰 틀만 잡은 정도입니다. 
