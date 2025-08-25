package com.promiseservice.service;

import com.promiseservice.dto.SmsNotificationRequest;
import com.promiseservice.dto.SmsNotificationResponse;
import com.promiseservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SMS 알림을 처리하는 서비스
 * 이유: 약속 관련 중요한 알림을 SMS로 전송하여 사용자가 놓치지 않도록 하고, 
 * 즉시성이 중요한 알림에 대해 푸시 알림의 대안으로 SMS 알림을 제공하기 위해
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final RestTemplate restTemplate;
    private final UserService userService;

    // SMS 서비스 기본 URL
    // 이유: 외부 SMS 서비스와의 통신을 위한 엔드포인트 설정
    @Value("${smsservice.base-url:http://localhost:8084}")
    private String smsServiceBaseUrl;

    // SMS 전송 API 경로
    // 이유: SMS 서비스의 메시지 전송 엔드포인트 경로 설정
    @Value("${smsservice.api.send:/api/sms/send}")
    private String smsSendApiPath;

    // SMS API 키
    // 이유: SMS 서비스 인증을 위한 API 키 설정
    @Value("${smsservice.api.key:demo-api-key}")
    private String smsApiKey;

    // 발신자 번호
    // 이유: SMS 발신자 번호를 설정하여 수신자가 발신처를 확인할 수 있도록 함
    @Value("${smsservice.sender.number:15441234}")
    private String senderNumber;

    /**
     * 사용자 ID 목록을 기반으로 SMS를 전송하는 메서드
     * 이유: 사용자 ID를 통해 전화번호를 조회하고 SMS를 전송하여 편리한 SMS 발송 기능 제공
     * 
     * @param smsRequest SMS 전송 요청 정보
     * @return SMS 전송 결과
     */
    public SmsNotificationResponse sendSmsToUsers(SmsNotificationRequest smsRequest) {
        log.info("사용자 ID 기반 SMS 전송 시작 - 대상 사용자: {}명", smsRequest.getRecipientUserIds().size());

        try {
            // 사용자 ID를 통해 전화번호 목록 조회
            // 이유: 사용자 정보에서 전화번호를 가져와 실제 SMS 전송이 가능하도록 함
            List<String> phoneNumbers = getPhoneNumbersFromUserIds(smsRequest.getRecipientUserIds());

            if (phoneNumbers.isEmpty()) {
                log.warn("전화번호를 찾을 수 없음 - 사용자 ID: {}", smsRequest.getRecipientUserIds());
                return new SmsNotificationResponse(new ArrayList<>(), new ArrayList<>());
            }

            // 전화번호로 SMS 전송
            return sendSmsToPhoneNumbers(phoneNumbers, smsRequest);

        } catch (Exception e) {
            log.error("사용자 ID 기반 SMS 전송 실패 - 에러: {}", e.getMessage());
            return createFailureResponse(smsRequest.getRecipientUserIds().stream()
                .map(String::valueOf).collect(Collectors.toList()), e.getMessage());
        }
    }

    /**
     * 전화번호 목록으로 직접 SMS를 전송하는 메서드
     * 이유: 전화번호를 직접 지정하여 SMS를 전송할 수 있는 유연한 기능 제공
     * 
     * @param phoneNumbers 전화번호 목록
     * @param smsRequest SMS 전송 요청 정보
     * @return SMS 전송 결과
     */
    public SmsNotificationResponse sendSmsToPhoneNumbers(List<String> phoneNumbers, SmsNotificationRequest smsRequest) {
        log.info("전화번호 기반 SMS 전송 시작 - 대상 번호: {}개", phoneNumbers.size());

        List<String> successfullyNotified = new ArrayList<>();
        List<String> failedToNotify = new ArrayList<>();
        Map<String, String> failureReasons = new HashMap<>();

        for (String phoneNumber : phoneNumbers) {
            try {
                // 전화번호 형식 검증
                // 이유: 잘못된 전화번호 형식으로 인한 SMS 전송 실패를 사전에 방지
                if (!smsRequest.isValidPhoneNumber(phoneNumber)) {
                    String reason = "잘못된 전화번호 형식";
                    failedToNotify.add(phoneNumber);
                    failureReasons.put(phoneNumber, reason);
                    log.warn("잘못된 전화번호 형식 - 번호: {}", phoneNumber);
                    continue;
                }

                // 개별 SMS 전송
                boolean success = sendSingleSms(phoneNumber, smsRequest);
                
                if (success) {
                    successfullyNotified.add(phoneNumber);
                    log.info("SMS 전송 성공 - 번호: {}", phoneNumber);
                } else {
                    failedToNotify.add(phoneNumber);
                    failureReasons.put(phoneNumber, "SMS 서비스 전송 실패");
                    log.warn("SMS 전송 실패 - 번호: {}", phoneNumber);
                }

            } catch (Exception e) {
                failedToNotify.add(phoneNumber);
                failureReasons.put(phoneNumber, e.getMessage());
                log.error("SMS 전송 중 예외 발생 - 번호: {}, 에러: {}", phoneNumber, e.getMessage());
            }
        }

        // 전송 결과 응답 생성
        SmsNotificationResponse response = new SmsNotificationResponse(successfullyNotified, failedToNotify);
        response.setFailureReasons(failureReasons);

        log.info("SMS 전송 완료 - 성공: {}건, 실패: {}건", successfullyNotified.size(), failedToNotify.size());
        return response;
    }

    /**
     * 긴급 SMS를 전송하는 메서드
     * 이유: 긴급한 약속 변경이나 취소 시 높은 우선순위로 SMS를 전송하여 즉시 알림 제공
     * 
     * @param recipientUserIds 수신자 사용자 ID 목록
     * @param message 긴급 메시지 내용
     * @param meetingId 관련 약속 ID
     * @return SMS 전송 결과
     */
    public SmsNotificationResponse sendUrgentSms(List<Long> recipientUserIds, String message, Long meetingId) {
        log.info("긴급 SMS 전송 시작 - 대상: {}명, 약속 ID: {}", recipientUserIds.size(), meetingId);

        SmsNotificationRequest urgentRequest = new SmsNotificationRequest();
        urgentRequest.setRecipientUserIds(recipientUserIds);
        urgentRequest.setMessage("[긴급] " + message);
        urgentRequest.setSmsType("URGENT");
        urgentRequest.setMeetingId(meetingId);
        urgentRequest.setSenderName("PromiseService 긴급알림");

        return sendSmsToUsers(urgentRequest);
    }

    /**
     * 사용자 ID 목록에서 전화번호 목록을 조회하는 메서드
     * 이유: 사용자 서비스를 통해 전화번호 정보를 가져와 SMS 전송이 가능하도록 함
     * 
     * @param userIds 사용자 ID 목록
     * @return 전화번호 목록
     */
    private List<String> getPhoneNumbersFromUserIds(List<Long> userIds) {
        List<String> phoneNumbers = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                // 사용자 정보 조회
                UserDto user = userService.getUserById(userId);
                
                if (user != null && user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                    phoneNumbers.add(user.getPhoneNumber());
                    log.debug("사용자 {}의 전화번호 조회 성공: {}", userId, user.getPhoneNumber());
                } else {
                    log.warn("사용자 {}의 전화번호 정보 없음", userId);
                }

            } catch (Exception e) {
                log.error("사용자 {} 정보 조회 실패: {}", userId, e.getMessage());
            }
        }

        return phoneNumbers;
    }

    /**
     * 개별 SMS를 전송하는 메서드
     * 이유: 외부 SMS 서비스 API를 호출하여 실제 SMS 전송을 수행하기 위해
     * 
     * @param phoneNumber 수신자 전화번호
     * @param smsRequest SMS 요청 정보
     * @return 전송 성공 여부
     */
    private boolean sendSingleSms(String phoneNumber, SmsNotificationRequest smsRequest) {
        try {
            // SMS 서비스 요청 데이터 생성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("to", phoneNumber);
            requestData.put("from", senderNumber);
            requestData.put("message", smsRequest.getMessage());
            requestData.put("type", smsRequest.getSmsType());
            requestData.put("senderName", smsRequest.getSenderName());
            
            // 관련 약속 정보 추가
            if (smsRequest.getMeetingId() != null) {
                requestData.put("meetingId", smsRequest.getMeetingId());
            }

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + smsApiKey);
            headers.set("X-API-Key", smsApiKey);

            // HTTP 요청 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            // SMS 서비스에 POST 요청 전송
            String url = smsServiceBaseUrl + smsSendApiPath;
            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);

            // 응답 확인
            if (response != null && "success".equals(response.get("status"))) {
                log.debug("SMS 전송 성공 - 번호: {}, 응답: {}", phoneNumber, response);
                return true;
            } else {
                log.warn("SMS 전송 실패 - 번호: {}, 응답: {}", phoneNumber, response);
                return false;
            }

        } catch (Exception e) {
            log.error("SMS 전송 API 호출 실패 - 번호: {}, 에러: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * 전송 실패 응답을 생성하는 메서드
     * 이유: 전송 실패 시 일관된 형태의 응답을 생성하여 오류 처리의 일관성 보장
     * 
     * @param recipients 수신자 목록
     * @param errorMessage 오류 메시지
     * @return 실패 응답
     */
    private SmsNotificationResponse createFailureResponse(List<String> recipients, String errorMessage) {
        SmsNotificationResponse response = new SmsNotificationResponse(new ArrayList<>(), recipients);
        
        Map<String, String> failureReasons = new HashMap<>();
        for (String recipient : recipients) {
            failureReasons.put(recipient, errorMessage);
        }
        response.setFailureReasons(failureReasons);
        
        return response;
    }

    /**
     * SMS 서비스 상태를 확인하는 메서드
     * 이유: SMS 서비스의 동작 상태를 확인하여 서비스 가용성을 모니터링하기 위해
     * 
     * @return SMS 서비스 상태
     */
    public boolean checkSmsServiceHealth() {
        try {
            String url = smsServiceBaseUrl + "/health";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + smsApiKey);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            boolean isHealthy = response != null && "UP".equals(response.get("status"));
            log.info("SMS 서비스 상태 확인 - 상태: {}", isHealthy ? "정상" : "오류");
            
            return isHealthy;
            
        } catch (Exception e) {
            log.error("SMS 서비스 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}

