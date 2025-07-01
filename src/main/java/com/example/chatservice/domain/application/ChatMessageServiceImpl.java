package com.example.chatservice.domain.application;

import com.example.chatservice.domain.dto.in.SendChatMessageReqDto;
import com.example.chatservice.domain.dto.out.ReadReceiptResDto;
import com.example.chatservice.domain.dto.out.SendChatMessageResDto;
import com.example.chatservice.domain.entiy.ChatMessage;
import com.example.chatservice.domain.infrastructure.ChatMessageRepository;
import com.example.chatservice.domain.infrastructure.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public SendChatMessageResDto sendMessage(SendChatMessageReqDto dto) {
        SendChatMessageResDto result = SendChatMessageResDto.from(chatMessageRepository.save(dto.toEntity()));
        chatRoomRepository.findByChatRoomUuid(dto.getChatRoomUuid())
                .ifPresent(room -> {
                    room.updateLastMessage(dto.getContent(), dto.getSentAt());
                    chatRoomRepository.save(room);
                });
        simpMessagingTemplate.convertAndSend("/queue/messages/" + dto.getChatRoomUuid(), dto);
        return result;
    }

    @Override
    public void markMessagesAsRead(String readerUuid, List<String> messageIds) {
        List<ChatMessage> messages = chatMessageRepository.findAllById(messageIds);

        for (ChatMessage message : messages) {
            // 본인이 수신자인 메시지만 처리
            if (!message.isRead() && message.getReceiverUuid().equals(readerUuid)) {
                message.setRead(true); // MongoDB에선 setter 또는 전체 객체 갱신 필요
                chatMessageRepository.save(message);

                // 읽음 이벤트 상대방에게 전송
                simpMessagingTemplate.convertAndSend(
                        "/queue/read/" + message.getSenderUuid(),
                        new ReadReceiptResDto(message.getId(), readerUuid)
                );
            }
        }
    }
}
