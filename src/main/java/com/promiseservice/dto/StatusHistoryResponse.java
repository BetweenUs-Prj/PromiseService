package com.promiseservice.dto;

import com.promiseservice.domain.entity.MeetingHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class StatusHistoryResponse {

    private Long id;
    private String action;
    private String details;
    private Long userId;
    private LocalDateTime timestamp;

    public static StatusHistoryResponse from(MeetingHistory history) {
        StatusHistoryResponse response = new StatusHistoryResponse();
        response.setId(history.getId());
        response.setAction(history.getAction().name());
        response.setDetails(history.getAction().getDisplayName()); // ActionType의 표시명 사용
        response.setUserId(history.getUserId());
        response.setTimestamp(history.getTimestamp());
        return response;
    }
}




