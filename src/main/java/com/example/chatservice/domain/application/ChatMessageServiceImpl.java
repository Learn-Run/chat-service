package com.example.chatservice.domain.application;

import com.example.chatservice.common.config.WebSocketEventListener;
import com.example.chatservice.domain.application.ChatEventService;
import com.example.chatservice.domain.dto.in.SendChatMessageReqDto;
import com.example.chatservice.domain.dto.out.SendChatMessageResDto;
import com.example.chatservice.domain.infrastructure.ChatMessageRepository;
import com.example.chatservice.domain.infrastructure.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketEventListener webSocketEventListener;
    private final ChatEventService chatEventService;

    @Override
    public SendChatMessageResDto sendMessage(SendChatMessageReqDto dto) {
        // 1. 메시지 저장
        SendChatMessageResDto result = SendChatMessageResDto.from(chatMessageRepository.save(dto.toEntity()));
        
        // 2. 채팅방 마지막 메시지 업데이트
        chatRoomRepository.findByChatRoomUuid(dto.getChatRoomUuid())
                .ifPresent(room -> {
                    room.updateLastMessage(dto.getContent(), dto.getSentAt());
                    chatRoomRepository.save(room);
                });
        
        // 3. 연결된 사용자들에게 메시지 전송
        sendMessageToConnectedUsers(dto, result);
        
        return result;
    }
    
    /**
     * 연결된 사용자들에게 메시지 전송
     */
    private void sendMessageToConnectedUsers(SendChatMessageReqDto dto, SendChatMessageResDto result) {
        String chatRoomUuid = dto.getChatRoomUuid();
        String receiverUuid = dto.getReceiverUuid();
        
        // 채팅방에 연결된 사용자 목록 조회
        Set<String> connectedUsers = webSocketEventListener.getConnectedUsersInChatRoom(chatRoomUuid);
        
        if (connectedUsers.isEmpty()) {
            log.info("채팅방 {}에 연결된 사용자가 없습니다. 메시지는 저장만 됩니다.", chatRoomUuid);
            return;
        }
        
        // 수신자가 연결되어 있는지 확인
        boolean receiverConnected = webSocketEventListener.isUserInChatRoom(chatRoomUuid, receiverUuid);
        
        if (receiverConnected) {
            // 수신자가 연결되어 있으면 실시간 전송
            log.info("수신자 {}가 연결되어 있어 실시간 메시지 전송", receiverUuid);
            simpMessagingTemplate.convertAndSend("/queue/messages/" + chatRoomUuid, dto);
            
            // 메시지 전달됨 이벤트 저장
            chatEventService.saveMessageDeliveredEvent(chatRoomUuid, receiverUuid, result.getMessageUuid());
        } else {
            // 수신자가 연결되어 있지 않으면 개인 메시지로 전송 (나중에 읽을 수 있도록)
            log.info("수신자 {}가 연결되어 있지 않아 개인 메시지로 전송", receiverUuid);
            simpMessagingTemplate.convertAndSendToUser(
                    receiverUuid, 
                    "/queue/private-messages", 
                    dto
            );
        }
        
        // 발신자에게도 전송 확인 메시지
        if (!dto.getSenderUuid().equals(receiverUuid)) {
            simpMessagingTemplate.convertAndSendToUser(
                    dto.getSenderUuid(),
                    "/queue/message-sent",
                    Map.of(
                            "messageUuid", result.getMessageUuid(),
                            "chatRoomUuid", chatRoomUuid,
                            "sentAt", dto.getSentAt(),
                            "delivered", receiverConnected
                    )
            );
        }
    }
}
