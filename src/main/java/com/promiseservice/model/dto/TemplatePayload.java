package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카카오톡 메시지 템플릿 페이로드 DTO
 * 이유: 카카오톡 메시지 전송 시 사용할 템플릿 정보를 구성하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class TemplatePayload {

    private String inviter;        // 초대자 이름
    private String date;          // 약속 날짜/시간
    private String place;         // 약속 장소
    private String meetingUrl;    // 약속 상세보기 URL
    private String title;         // 약속 제목
    private String description;   // 약속 설명

    /**
     * 생성자
     * 이유: 필수 정보로 템플릿 페이로드를 편리하게 생성하기 위해
     * 
     * @param inviter 초대자 이름
     * @param date 약속 날짜/시간
     * @param place 약속 장소
     * @param meetingUrl 약속 상세보기 URL
     */
    public TemplatePayload(String inviter, String date, String place, String meetingUrl) {
        this.inviter = inviter;
        this.date = date;
        this.place = place;
        this.meetingUrl = meetingUrl;
    }

    /**
     * 전체 정보를 포함한 생성자
     * 이유: 모든 템플릿 정보를 한 번에 설정하기 위해
     * 
     * @param inviter 초대자 이름
     * @param date 약속 날짜/시간
     * @param place 약속 장소
     * @param meetingUrl 약속 상세보기 URL
     * @param title 약속 제목
     * @param description 약속 설명
     */
    public TemplatePayload(String inviter, String date, String place, String meetingUrl, 
                          String title, String description) {
        this.inviter = inviter;
        this.date = date;
        this.place = place;
        this.meetingUrl = meetingUrl;
        this.title = title;
        this.description = description;
    }

    /**
     * 템플릿이 유효한지 확인하는 메서드
     * 이유: 카카오톡 메시지 전송 전에 필수 정보가 모두 포함되어 있는지 확인하기 위해
     * 
     * @return 템플릿 유효 여부
     */
    public boolean isValid() {
        return inviter != null && !inviter.trim().isEmpty() &&
               date != null && !date.trim().isEmpty() &&
               place != null && !place.trim().isEmpty();
    }

    /**
     * 간단한 메시지 텍스트를 생성하는 메서드
     * 이유: 카카오톡 메시지 전송 실패 시 대체 메시지나 로깅용 텍스트를 생성하기 위해
     * 
     * @return 간단한 메시지 텍스트
     */
    public String toSimpleText() {
        StringBuilder sb = new StringBuilder();
        sb.append(inviter).append("님의 약속 초대");
        if (title != null) {
            sb.append("\n제목: ").append(title);
        }
        sb.append("\n일시: ").append(date);
        sb.append("\n장소: ").append(place);
        if (meetingUrl != null) {
            sb.append("\n상세보기: ").append(meetingUrl);
        }
        return sb.toString();
    }
}
