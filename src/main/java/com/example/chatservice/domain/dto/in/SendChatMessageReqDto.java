package com.example.chatservice.domain.dto.in;

import com.example.chatservice.domain.entiy.ChatMessage;
import com.example.chatservice.domain.vo.in.SendChatMessageReqVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class SendChatMessageReqDto {
    private String chatRoomUuid;
    private String senderUuid;
    private String receiverUuid;

    private String content;
    private LocalDateTime sentAt;

    private boolean read;

    @Builder
    public SendChatMessageReqDto(
            String chatRoomUuid, String senderUuid, String receiverUuid, String content, LocalDateTime sentAt,
            boolean read
    ) {
        this.chatRoomUuid = chatRoomUuid;
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.content = content;
        this.sentAt = sentAt;
        this.read = read;
    }

    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .messageUuId(UUID.randomUUID().toString())
                .chatRoomUuid(chatRoomUuid)
                .senderUuid(senderUuid)
                .receiverUuid(receiverUuid)
                .content(content)
                .sentAt(LocalDateTime.now())
                .read(false)
                .messageType(com.example.chatservice.domain.enums.MessageType.CHAT)
                .eventData(null)
                .build();
    }

    public static SendChatMessageReqDto of(String senderUuid, SendChatMessageReqVo vo) {
        return SendChatMessageReqDto.builder()
                .chatRoomUuid(vo.getChatRoomUuid())
                .senderUuid(senderUuid)
                .receiverUuid(vo.getReceiverUuid())
                .content(vo.getContent())
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();
    }
}
