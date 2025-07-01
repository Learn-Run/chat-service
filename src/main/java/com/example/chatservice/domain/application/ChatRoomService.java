package com.example.chatservice.domain.application;

import com.example.chatservice.common.response.CursorPage;
import com.example.chatservice.domain.dto.in.ChatMessageReqDto;
import com.example.chatservice.domain.dto.in.ChatRoomListReqDto;
import com.example.chatservice.domain.dto.in.CreateChatRoomReqDto;
import com.example.chatservice.domain.dto.in.MarkMessageAsReadReqDto;
import com.example.chatservice.domain.dto.out.ChatRoomListResDto;
import com.example.chatservice.domain.dto.out.ChatMessageResDto;
import com.example.chatservice.domain.dto.out.CreateChatRoomResDto;

import java.util.Map;

public interface ChatRoomService {

    CreateChatRoomResDto createOrGetRoom(CreateChatRoomReqDto dto);

    void markUnreadMessagesAsRead(MarkMessageAsReadReqDto dto);

    CursorPage<ChatRoomListResDto> getChatRoomList(String memberUuid, ChatRoomListReqDto chatRoomListReqDto);

    ChatRoomListResDto getChatRoom(String memberUuid,String chatRoomUuid);

    CursorPage<ChatMessageResDto> getChatMessages(String chatRoomUuid, ChatMessageReqDto dto);
    
    Map<String, Object> getChatRoomStatus(String chatRoomUuid, String memberUuid);
}
