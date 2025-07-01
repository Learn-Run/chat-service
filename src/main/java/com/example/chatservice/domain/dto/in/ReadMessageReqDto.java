package com.example.chatservice.domain.dto.in;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ReadMessageReqDto {
    private String chatRoomUuid;
    private List<Long> messageIds;

    @Builder
    public ReadMessageReqDto(String chatRoomUuid, List<Long> messageIds) {
        this.chatRoomUuid = chatRoomUuid;
        this.messageIds = messageIds;
    }
}
