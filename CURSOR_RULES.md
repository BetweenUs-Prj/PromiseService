# 🎯 PromiseService 커서룰 (Cursor Rules)

## **📝 코드 주석 작성 규칙**

### **1. 클래스 레벨 주석**

**모든 클래스는 다음 형식의 주석을 가져야 합니다:**

```java
/**
 * 클래스의 목적과 역할을 설명하는 간단한 설명
 * 이유: 이 클래스가 왜 존재하고 어떤 문제를 해결하는지 명확히 하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
```

**예시:**
```java
/**
 * 약속(미팅) 정보를 저장하고 관리하는 JPA 엔티티
 * 이유: 사용자가 생성한 약속의 상세 정보, 참여자 관리, 상태 추적을 통합적으로 처리하여
 * 약속 생성부터 완료까지의 전체 생명주기를 체계적으로 관리하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
```

### **2. 필드 레벨 주석**

**모든 필드는 다음 형식의 주석을 가져야 합니다:**

```java
/**
 * 필드의 의미와 용도
 * 이유: 이 필드가 왜 필요하고 어떻게 사용되는지 명확히 하기 위해
 * 추가적인 제약사항이나 비즈니스 규칙이 있다면 함께 설명
 */
```

**예시:**
```java
/**
 * 약속 제목
 * 이유: 사용자가 약속을 쉽게 식별하고 구분할 수 있도록 하기 위해
 * 알림 메시지 및 목록 화면에서 주요 식별 정보로 활용
 */
@Column(nullable = false, length = 255)
private String title;
```

### **3. 메서드 레벨 주석**

**모든 public/protected 메서드는 다음 형식의 주석을 가져야 합니다:**

```java
/**
 * 메서드의 목적과 동작 방식
 * 이유: 이 메서드가 왜 존재하고 어떤 문제를 해결하는지 명확히 하기 위해
 * 
 * @param paramName 매개변수의 의미와 제약사항
 * @return 반환값의 의미와 형식
 * @throws ExceptionType 예외가 발생하는 경우와 조건
 */
```

**예시:**
```java
/**
 * 약속 생성 요청의 유효성을 검증하는 메서드
 * 이유: 서버에서 요청 데이터의 유효성을 검증하여 잘못된 데이터로 인한 오류를 사전에 방지하고
 * 비즈니스 규칙을 준수하는 데이터만 처리하기 위해
 * 
 * @return 유효성 검증 결과 (true: 유효함, false: 유효하지 않음)
 */
public boolean isValid() {
    // 구현 내용...
}
```

### **4. 내부 로직 주석**

**복잡한 로직이나 비즈니스 규칙이 있는 경우 인라인 주석을 추가:**

```java
// Stream API를 활용한 효율적인 필터링 및 카운팅
// 이유: 대량의 참여자 데이터도 효율적으로 처리하고 가독성을 높이기 위해
return (int) participants.stream()
        .filter(participant -> participant.getResponse() == MeetingParticipant.ResponseStatus.ACCEPTED)
        .count();
```

### **5. 열거형(Enum) 주석**

**열거형 값과 메서드에 상세한 주석 추가:**

```java
public enum MeetingStatus {
    /** 대기 중: 방장이 약속을 생성했지만 아직 확정하지 않은 상태 */
    WAITING("대기 중"),
    /** 확정: 방장이 약속을 확정하여 참여자들에게 알림이 발송된 상태 */
    CONFIRMED("확정");
    
    /**
     * 상태의 사용자 친화적인 표시명을 반환하는 메서드
     * 이유: 알림 메시지, UI 화면에서 약속 상태를 사용자가 이해하기 쉽게 표시하기 위해
     * 
     * @return 상태의 한글 표시명
     */
    public String getDisplayName() {
        return this.displayName;
    }
}
```

### **6. DTO 클래스 주석**

**DTO 클래스는 API 명세서와 연관된 상세한 주석 추가:**

```java
/**
 * 약속 생성 요청 DTO
 * 이유: 클라이언트로부터 약속 생성에 필요한 정보를 받아서 서버에서 처리하기 위해
 * 
 * API 명세서: POST /api/meetings
 * 요청 예시:
 * {
 *   "title": "저녁 모임",
 *   "placeId": 901,
 *   "scheduledAt": "2025-09-05T19:30:00+09:00",
 *   "maxParticipants": 10,
 *   "memo": "7시까지 오세요"
 * }
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
```

### **7. 컨트롤러 메서드 주석**

**API 엔드포인트에 대한 상세한 설명과 예시 추가:**

```java
/**
 * 약속방 생성 API
 * 이유: 사용자가 새로운 약속을 생성하고 다른 사용자들을 초대할 수 있도록 하기 위해
 * 
 * HTTP Method: POST
 * Path: /api/meetings
 * Headers: 
 *   - Content-Type: application/json
 *   - X-User-Id: {사용자ID} (방장)
 * 
 * Request Body: MeetingCreateRequest
 * Response: MeetingCreateResponse (201 Created)
 * 
 * 비즈니스 로직:
 * 1. 사용자 존재 여부 확인
 * 2. 약속 정보 유효성 검증
 * 3. 약속 및 참여자 정보 저장
 * 4. 초대자들에게 알림 발송
 * 5. 생성된 약속 정보 반환
 * 
 * @param request 약속 생성 요청 정보
 * @param hostId 방장 사용자 ID (Header에서 추출)
 * @return 생성된 약속 정보
 */
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public MeetingCreateResponse createMeeting(@RequestBody MeetingCreateRequest request,
                                         @RequestHeader("X-User-Id") Long hostId) {
    // 구현 내용...
}
```

### **8. 서비스 메서드 주석**

**비즈니스 로직의 핵심 단계와 예외 상황을 명확히 설명:**

```java
/**
 * 약속 생성 및 초대 처리
 * 이유: 약속 생성부터 참여자 초대까지의 전체 프로세스를 트랜잭션으로 관리하여
 * 데이터 일관성을 보장하고 실패 시 롤백을 수행하기 위해
 * 
 * 처리 단계:
 * 1. 약속 정보 유효성 검증
 * 2. 약속 엔티티 생성 및 저장
 * 3. 방장을 첫 번째 참여자로 추가
 * 4. 초대 대상자들을 참여자로 추가
 * 5. 약속 생성 완료 이벤트 발행
 * 
 * 예외 상황:
 * - 사용자가 존재하지 않는 경우: RuntimeException
 * - 최대 참여자 수 초과: RuntimeException
 * - 잘못된 약속 시간: IllegalArgumentException
 * 
 * @param request 약속 생성 요청
 * @param hostId 방장 사용자 ID
 * @return 생성된 약속 응답 정보
 * @throws RuntimeException 사용자 존재하지 않음, 인원 수 초과 등
 * @throws IllegalArgumentException 잘못된 입력값
 */
@Transactional
public MeetingCreateResponse createMeeting(MeetingCreateRequest request, Long hostId) {
    // 구현 내용...
}
```

### **9. 리포지토리 메서드 주석**

**쿼리 메서드의 목적과 성능 고려사항을 명시:**

```java
/**
 * 특정 사용자가 참여한 약속 목록을 조회
 * 이유: 사용자의 약속 이력을 조회하여 개인화된 서비스를 제공하기 위해
 * 
 * 성능 고려사항:
 * - meeting_id, user_id에 복합 인덱스 필요
 * - 페이징 처리로 대량 데이터 조회 방지
 * - N+1 문제 방지를 위한 fetch join 고려
 * 
 * @param userId 조회할 사용자 ID
 * @param pageable 페이징 및 정렬 정보
 * @return 사용자가 참여한 약속 페이지
 */
Page<Meeting> findByParticipantsUserId(Long userId, Pageable pageable);
```

### **10. 설정 클래스 주석**

**설정의 목적과 영향 범위를 명확히 설명:**

```java
/**
 * CORS 설정 클래스
 * 이유: 프론트엔드 애플리케이션에서 API 서버에 접근할 수 있도록
 * Cross-Origin Resource Sharing 정책을 설정하기 위해
 * 
 * 설정 내용:
 * - 허용된 오리진: localhost:3000, localhost:8080
 * - 허용된 HTTP 메서드: GET, POST, PUT, DELETE, OPTIONS
 * - 허용된 헤더: 모든 헤더
 * - 자격 증명 포함: true (쿠키, 인증 헤더 등)
 * 
 * 보안 고려사항:
 * - 프로덕션 환경에서는 허용된 오리진을 제한해야 함
 * - 민감한 헤더 노출 방지를 위한 헤더 필터링 필요
 */
@Configuration
public class CorsConfig {
    // 구현 내용...
}
```

## **🚀 주석 작성 체크리스트**

### **클래스 작성 시:**
- [ ] 클래스의 목적과 역할을 명확히 설명
- [ ] 이 클래스가 왜 필요한지 이유 명시
- [ ] @author, @since 태그 추가

### **필드 작성 시:**
- [ ] 필드의 의미와 용도 설명
- [ ] 제약사항이나 비즈니스 규칙 명시
- [ ] null 허용 여부와 기본값 설명

### **메서드 작성 시:**
- [ ] 메서드의 목적과 동작 방식 설명
- [ ] 매개변수와 반환값의 의미 명시
- [ ] 예외 상황과 발생 조건 설명
- [ ] 복잡한 로직에 대한 단계별 주석 추가

### **API 작성 시:**
- [ ] HTTP 메서드와 경로 명시
- [ ] 요청/응답 예시 제공
- [ ] 비즈니스 로직 단계 설명
- [ ] 예외 상황과 에러 코드 명시

### **설정 작성 시:**
- [ ] 설정의 목적과 영향 범위 설명
- [ ] 보안 고려사항 명시
- [ ] 성능 영향과 최적화 방안 제시

## **💡 주석 작성 팁**

1. **"왜(Why)"에 집중**: 무엇을 하는지보다 왜 하는지 설명
2. **구체적인 예시**: API 사용법, 비즈니스 시나리오 등
3. **예외 상황**: 실패할 수 있는 경우와 처리 방법
4. **성능 고려사항**: 인덱스, 쿼리 최적화 등
5. **보안 주의사항**: 인증, 권한, 데이터 검증 등

## **📚 참고 자료**

- [JavaDoc 가이드](https://docs.oracle.com/javase/8/docs/technotes/guides/javadoc/)
- [Spring Boot API 문서화](https://springdoc.org/)
- [REST API 설계 가이드](https://restfulapi.net/)

---

**이 커서룰을 따라 작성된 코드는 유지보수성과 가독성이 크게 향상되어, 팀원들이 코드의 의도와 목적을 쉽게 이해할 수 있습니다!** ✨
