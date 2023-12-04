## Section 3. API 개발 고급 - 지연 로딩과 조회 성능 최적화

주문 + 배송정보 + 회원을 조회하는 API를 만들자.

지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결해보자.



### 간단한 주문 조회 V1: 엔티티를 직접 노출

- OrderSimpleApiController

  ``` java
  
  /**
   * xToOne(ManyToOne, OneToOne) 관계 최적화 Order Order -> Member Order -> Delivery
   */
  @RestController
  @RequiredArgsConstructor
  public class OrderSimpleApiController {
  
      private final OrderRepository orderRepository;
  
      /**
       * V1. 엔티티 직접 노출 - Hibernate5Module 모듈 등록, LAZY = null 처리 - 양방향 관계 문제 발생 -> @JsonIgnore
       */
      @GetMapping("/api/v1/simple-orders")
      public List<Order> ordersV1() {
          List<Order> all = orderRepository.findAllByString(new OrderSearch());
          for (Order order : all) {
              order.getMember().getName(); // LAZY 강제 초기화
              order.getDelivery().getAddress(); // LAZY 강제 초기화
          }
          return all;
      }
  }
  
  ```

  - 엔티티를 직접 노출하는 것은 좋지 않다. (앞 장 참고)

    - API 스펙 변경시 곤란..
    - 성능 문제 => OrderItems 같은 경우에는 응답에 포함되지 않아도 되는데, 추가 쿼리 실행으로 또 땡겨오기 때문

  - `ordrer` -> `member` 와 `order` -> `delivery` 는 지연 로딩이다. 따라서 실제 엔티티 대신에 프록시 존재

    - 지연로딩: DB에서 긁어오는게 아니라 Proxy 객체를 가져온다. (ByteBuddyInterceptor) 

  - jackson 라이브러리는 기본적으로 이 프록시 객체를 json 으로 어떻게 생성해야 하는지 모름 => 예외 발생

  - `Hibernate5Module` 을 스프링 빈으로 등록하면 해결 (스프링 부트 사용중)

    - dependency 후가 후 빈으로 등록

      ``` java
      @Bean
      Hibernate5Module hibernate5Module() {
      		return new Hibernate5Module();
      }
      ```

> 주의: 스프링 부트 3.0 이상이면 `Hibernate5Module` 대신에 `Hibernate5JakartaModule` 을 사용해야 한다.

> 주의: 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭! 한곳을 @JsonIgnore 처리해야 한다. 안그러면 양쪽을 서로 호출하면서 무한 루프가 걸린다. (하지만 엔티티를 직접 노출하는 방식은 절대 금지!)

> 참고: 앞에서 계속 강조했듯이 정말 간단한 애플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다. 따라서 `Hibernate5Module` 을 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.

> 주의: 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EAGER) 으로 설정하면 안된다! 즉시 로딩 떄문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다. 즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워진다. 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 패치 조인(fetch join) 을 사용해라!(V3에서 설명)







