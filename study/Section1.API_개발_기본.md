## Section 1. API 개발 기본

### 회원 등록 API

- `javax.validation` 을 사용하여 각 오브젝트를 컨트롤러 단에서 validation 을 진행할 수 있음

  ``` java
  @PostMapping("/api/v1/members")
  public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
      Long id = memberService.join(member);
      return new CreateMemberResponse(id);
  }
  ```

  ``` java
  @Entity
  @Getter
  @Setter
  public class Member {
  
      @Id
      @GeneratedValue
      @Column(name = "member_id")
      private Long id;
      
      @NotEmpty // javax.validation
      private String name;
    
    	...
  }
  
  ```

- 하지만 엔티티 자체를 컨트롤러의 @RequestBody 로 받는 방식은 좋지 않다.

  - API 별로 validation 처리가 다를 수 있다.
  - API 스펙(Member 엔티티의 멤버 변수명 변경 등등)이 변경될 경우, 유연하게 대응하지 못하고 최악의 경우 장애까지로 번질 수 있다.

- API 를 만들 때는 엔티티 자체를 파라미터로 받지 않도록 설계해야하며, 웹 상에 엔티티 자체를 노출시켜서는 안된다.

  ``` java
  @PostMapping("/api/v2/members")
  public CreateMemberResponse saveMemberV2(
      @RequestBody @Valid CreateMemberRequest createMemberRequest) {
  
      Member member = new Member();
      member.setName(createMemberRequest.getName());
  
      Long id = memberService.join(member);
      return new CreateMemberResponse(id);
  }
  ```

  - 장점 1. entity 에 대한 스펙이 변경되더라도 API 자체에 대한 영향은 전혀 받지 않는다.
  - 장점 2. CreateMemberRequest DTO 를 통해서 API 스펙이 정리가 된다.
  - 장점 3. CreateMemberRequest DTO 에 validation 로직을 넣어서 API 별로 validation 처리를 따로 가져갈 수 있다.

- <u>**결론: API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다**</u>



### 회원 수정 API

- 멱등성: 연산을 여러번 적용하더라도 결과가 달라지지 않는 성질, 연산을 여러 번 반복하여도 한 번만 수행된 것과 같은 성질

``` java
@PutMapping("/api/v2/members/{id}")
public UpdateMemberResponse updateMemberV2(
    @PathVariable("id") Long id,
    @RequestBody @Valid UpdateMemberRequest request) {

    // member 객체를 그대로 return 해도 가능은 하지만 CQRS 패턴(명령과 쿼리 분리) 에 따라 그냥 void 로 둔다.
    memberService.update(id, request.getName());
    Member findMember = memberService.findOne(id);
    return new UpdateMemberResponse(findMember.getId(), findMember.getName());
}

@Data
static class UpdateMemberRequest {
    private String name;
}

@Data
@AllArgsConstructor
static class UpdateMemberResponse {
    private Long id;
    private String name;
}
```

``` java
@Transactional
public void update(Long id, String name) {
    Member member = memberRepository.findOne(id); // 변경 감지
    member.setName(name);
    // member 객체를 그대로 return 해도 가능은 하지만 CQRS 패턴(명령과 쿼리 분리) 에 따라 그냥 void 로 둔다.
}
```

> 회원 수정 API `updateMemberV2` 은 회원 정보를 부분 업데이트 한다. 여기서 PUT 방식을 사용했는데, PUT Method는 REST API 스타일 상 전체 업데이트를 할 때 사용하는 것이 맞다. 부분 업데이트를 하려면 PATCH 를 사용하거나 POST를 사용하는 것이 REST 스타일에 맞다.



### 회원 조회 API

- 회원조회 V1: 응답 값으로 엔티티를 직접 외부에 노출

  ``` java
  @GetMapping("/api/v1/members")
  public List<Member> membersV1() {
      return memberService.findMembers();
  }
  ```

  - 문제점
    - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
    - 기본적으로 엔티티의 모든 값이 노출된다.
    - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
    - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
    - 엔티티가 변경되면 API 스펙이 변한다.
    - 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어렵다. (별도의 Result 클래스 생성으로 해결)
  - 결론
    - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.

- 회원조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO 사용

  ``` java
  @GetMapping("/api/v2/members")
  public Result membersV2() {
      List<Member> findMembers = memberService.findMembers();
      // 엔티티 -> DTO 변환
      List<MemberDto> collect = findMembers.stream()
          .map(m -> new MemberDto(m.getName()))
          .collect(Collectors.toList());
  
      return new Result(collect);
  }
  
  @Data
  @AllArgsConstructor
  static class Result<T> {
      private T data;
  }
  
  @Data
  @AllArgsConstructor
  static class MemberDto {
      private String name;
  }
  ```

  - 엔티티를 DTO로 변환해서 반환한다.
  - 엔티티가 변해도 API 스펙이 변경되지 않는다.
  - 추가로 `Result` 클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가할 수 있다.





