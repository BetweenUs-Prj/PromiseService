package com.promiseservice.event;

/**
 * 약속 생성 완료 이벤트
 * 이유: 트랜잭션 커밋 이후에 알림을 발송하기 위해
 */
public record MeetingCreatedEvent(Long meetingId) {
}
