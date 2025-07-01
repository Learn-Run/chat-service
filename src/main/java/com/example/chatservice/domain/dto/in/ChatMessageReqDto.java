package com.example.chatservice.domain.dto.in;

import com.example.chatservice.domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMessageReqDto {

    private String senderUuid;
    private LocalDateTime cursor;
    private Integer size;
    private List<MessageType> messageTypes; // 특정 타입의 메시지만 조회
    private Boolean includeSystemEvents; // 시스템 이벤트 포함 여부

}
