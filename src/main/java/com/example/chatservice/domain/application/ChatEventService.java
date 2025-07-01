package com.example.chatservice.domain.application;

import com.example.chatservice.domain.enums.MessageType;
import com.example.chatservice.domain.entiy.ChatMessage;
import com.example.chatservice.domain.infrastructure.ChatMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEventService {

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자 입장 이벤트 저장
     */
    public void saveUserJoinedEvent(String chatRoomUuid, String userUuid) {
        Map<String, Object> eventData = Map.of(
                "userUuid", userUuid,
                "action", "joined",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, userUuid, null, MessageType.USER_JOINED, eventData);
    }

    /**
     * 사용자 퇴장 이벤트 저장
     */
    public void saveUserLeftEvent(String chatRoomUuid, String userUuid) {
        Map<String, Object> eventData = Map.of(
                "userUuid", userUuid,
                "action", "left",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, userUuid, null, MessageType.USER_LEFT, eventData);
    }

    /**
     * 사용자 연결 이벤트 저장
     */
    public void saveUserConnectedEvent(String chatRoomUuid, String userUuid) {
        Map<String, Object> eventData = Map.of(
                "userUuid", userUuid,
                "action", "connected",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, userUuid, null, MessageType.USER_CONNECTED, eventData);
    }

    /**
     * 사용자 연결 해제 이벤트 저장
     */
    public void saveUserDisconnectedEvent(String chatRoomUuid, String userUuid) {
        Map<String, Object> eventData = Map.of(
                "userUuid", userUuid,
                "action", "disconnected",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, userUuid, null, MessageType.USER_DISCONNECTED, eventData);
    }

    /**
     * 채팅방 생성 이벤트 저장
     */
    public void saveRoomCreatedEvent(String chatRoomUuid, String creatorUuid, String participantAUuid, String participantBUuid) {
        Map<String, Object> eventData = Map.of(
                "creatorUuid", creatorUuid,
                "participantAUuid", participantAUuid,
                "participantBUuid", participantBUuid,
                "action", "room_created",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, creatorUuid, null, MessageType.ROOM_CREATED, eventData);
    }

    /**
     * 메시지 읽음 이벤트 저장
     */
    public void saveMessageReadEvent(String chatRoomUuid, String readerUuid, String messageUuid) {
        Map<String, Object> eventData = Map.of(
                "readerUuid", readerUuid,
                "messageUuid", messageUuid,
                "action", "message_read",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, readerUuid, null, MessageType.MESSAGE_READ, eventData);
    }

    /**
     * 메시지 전달됨 이벤트 저장
     */
    public void saveMessageDeliveredEvent(String chatRoomUuid, String receiverUuid, String messageUuid) {
        Map<String, Object> eventData = Map.of(
                "receiverUuid", receiverUuid,
                "messageUuid", messageUuid,
                "action", "message_delivered",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, null, receiverUuid, MessageType.MESSAGE_DELIVERED, eventData);
    }

    /**
     * 시스템 공지 저장
     */
    public void saveSystemNotice(String chatRoomUuid, String content, String adminUuid) {
        Map<String, Object> eventData = Map.of(
                "adminUuid", adminUuid,
                "action", "system_notice",
                "timestamp", LocalDateTime.now()
        );
        
        saveSystemEvent(chatRoomUuid, adminUuid, null, MessageType.SYSTEM_NOTICE, eventData, content);
    }

    /**
     * 시스템 이벤트 저장 공통 메서드
     */
    private void saveSystemEvent(String chatRoomUuid, String senderUuid, String receiverUuid, 
                               MessageType messageType, Map<String, Object> eventData) {
        saveSystemEvent(chatRoomUuid, senderUuid, receiverUuid, messageType, eventData, null);
    }

    /**
     * 시스템 이벤트 저장 공통 메서드 (커스텀 컨텐츠 포함)
     */
    private void saveSystemEvent(String chatRoomUuid, String senderUuid, String receiverUuid, 
                               MessageType messageType, Map<String, Object> eventData, String customContent) {
        try {
            String eventDataJson = objectMapper.writeValueAsString(eventData);
            String content = customContent != null ? customContent : messageType.getDescription();
            
            ChatMessage eventMessage = ChatMessage.builder()
                    .messageUuId(UUID.randomUUID().toString())
                    .chatRoomUuid(chatRoomUuid)
                    .senderUuid(senderUuid)
                    .receiverUuid(receiverUuid)
                    .content(content)
                    .sentAt(LocalDateTime.now())
                    .read(false)
                    .messageType(messageType)
                    .eventData(eventDataJson)
                    .build();
            
            chatMessageRepository.save(eventMessage);
            log.info("시스템 이벤트 저장 완료: type={}, chatRoomUuid={}, userUuid={}", 
                    messageType, chatRoomUuid, senderUuid != null ? senderUuid : receiverUuid);
            
        } catch (JsonProcessingException e) {
            log.error("이벤트 데이터 JSON 변환 실패: {}", e.getMessage(), e);
        }
    }
} 