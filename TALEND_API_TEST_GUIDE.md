# 🚀 Talend API Tester로 JSON API 테스트하기

## 📋 준비 사항

### 1. Talend API Tester 설치
- **Chrome 확장 프로그램**: [Talend API Tester](https://chrome.google.com/webstore/detail/talend-api-tester-free-ed/aejoelaoggembcahagimdiliamlcdmfm)
- **무료 버전** 사용 가능

### 2. 서버 실행 확인
```bash
# 서버가 실행 중인지 확인
curl http://localhost:8080
```

## 🎯 Talend API Tester 설정 방법

### **1단계: Collection Import**
1. Talend API Tester 열기
2. **Import** 버튼 클릭
3. `talend-api-test.json` 파일 선택
4. **Import Collection** 실행

### **2단계: Environment Variables 설정**
```json
{
  "baseUrl": "http://localhost:8080",
  "userId": "123",
  "meetingId": "1"
}
```

### **3단계: 테스트 실행 순서**

#### ✅ **기본 연결 테스트**
1. **"1. 서버 상태 확인"** 실행
   - **Expected**: 200 OK 응답
   - **Response**: HTML 또는 JSON

#### ✅ **간단한 JSON GET 테스트**
2. **"2. 사용자 존재 확인 (JSON)"** 실행
   - **Expected**: 200 OK
   - **JSON Response**:
   ```json
   {
     "userIdValue": 123,
     "existsValue": true
   }
   ```

#### ✅ **복잡한 JSON POST 테스트**
3. **"3. 약속 생성 (JSON POST)"** 실행
   - **Expected**: 201 Created
   - **JSON Response**:
   ```json
   {
     "id": 1,
     "hostId": 123,
     "title": "Talend API Tester 약속",
     "status": "WAITING",
     "participants": [
       {
         "userId": 123,
         "response": "ACCEPTED"
       }
     ]
   }
   ```

#### ✅ **중첩 JSON 구조 테스트**
4. **"4. 약속 목록 조회 (JSON)"** 실행
   - **Expected**: 200 OK
   - **JSON Array Response**

## 🔍 JSON 검증 포인트

### **Request 검증**
- ✅ **Content-Type**: `application/json`
- ✅ **Accept**: `application/json`
- ✅ **X-User-ID**: 헤더 포함
- ✅ **JSON 문법**: 유효한 JSON 형식

### **Response 검증**
- ✅ **Status Code**: 200, 201, 400, 404 등
- ✅ **Content-Type**: `application/json`
- ✅ **JSON 구조**: 예상된 필드 포함
- ✅ **Data Types**: 문자열, 숫자, 배열, 객체

## 🧪 고급 JSON 테스트 시나리오

### **1. 배열 처리 테스트**
- **Test**: "6. 참여자 초대 (JSON POST)"
- **JSON**:
```json
{
  "participantUserIds": [101, 102, 103]
}
```

### **2. 중첩 객체 테스트**
- **Test**: "3. 약속 생성 (JSON POST)"
- **JSON**:
```json
{
  "locationCoordinates": "{\"lat\": 37.498095, \"lng\": 127.027621}"
}
```

### **3. 검색 필터 테스트**
- **Test**: "8. 약속 검색 (JSON POST)"
- **복잡한 JSON 조건**:
```json
{
  "title": "Talend",
  "status": "WAITING",
  "startDate": "2025-08-19T00:00:00",
  "page": 0,
  "size": 10,
  "sortBy": "meetingTime"
}
```

### **4. 에러 응답 테스트**
- **Test**: "9. 에러 테스트 - 잘못된 JSON"
- **Expected**: 400 Bad Request
- **Error JSON**:
```json
{
  "timestamp": "2025-08-19T15:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed"
}
```

## 📊 Talend API Tester 활용 팁

### **1. Tests 탭 활용**
```javascript
// JSON 응답 검증
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has JSON body", function () {
    pm.response.to.be.json;
});

pm.test("Meeting created successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.title).to.eql("Talend API Tester 약속");
    pm.expect(jsonData.status).to.eql("WAITING");
});
```

### **2. Variables 자동 설정**
```javascript
// 응답에서 meetingId 추출하여 변수로 설정
pm.test("Extract meeting ID", function () {
    var jsonData = pm.response.json();
    pm.environment.set("meetingId", jsonData.id);
});
```

### **3. Pre-request Script**
```javascript
// 동적 데이터 생성
var timestamp = new Date().toISOString();
pm.environment.set("currentTime", timestamp);
```

## 🎯 실행 결과 확인 방법

### **✅ 성공 케이스**
- **Status**: 200/201 ✅
- **Response Time**: < 1000ms ✅
- **Content-Type**: application/json ✅
- **JSON Schema**: 유효 ✅

### **❌ 실패 케이스**
- **Status**: 400/404/500 ❌
- **Error Message**: 명확한 에러 설명
- **JSON Format**: 에러 응답도 JSON 형식

## 🔄 연속 테스트 시나리오

1. **약속 생성** → `meetingId` 저장
2. **참여자 초대** → 저장된 `meetingId` 사용
3. **상태 변경** → 동일한 `meetingId` 사용
4. **상세 조회** → 변경된 상태 확인

## 📈 성능 테스트

### **응답 시간 모니터링**
- **Target**: < 500ms (일반 요청)
- **Target**: < 1000ms (복잡한 검색)

### **동시 요청 테스트**
- Talend API Tester의 **Runner** 기능 활용
- 동일한 요청을 여러 번 실행하여 성능 측정

---

## 🎉 테스트 완료 후 확인사항

✅ **JSON 요청/응답 정상 동작**  
✅ **에러 처리 적절히 동작**  
✅ **중첩 JSON 구조 처리**  
✅ **배열 데이터 처리**  
✅ **날짜 형식 처리**  
✅ **페이지네이션 동작**

**Talend API Tester로 모든 JSON API가 정상 동작하면 성공! 🎊**









