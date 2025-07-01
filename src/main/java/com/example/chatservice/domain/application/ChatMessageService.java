package com.example.chatservice.domain.application;

import com.example.chatservice.domain.dto.in.SendChatMessageReqDto;
import com.example.chatservice.domain.dto.out.SendChatMessageResDto;

import java.util.List;

public interface ChatMessageService {

    SendChatMessageResDto sendMessage(SendChatMessageReqDto dto);

    void markMessagesAsRead(String readerUuid, List<String> messageIds);
}
