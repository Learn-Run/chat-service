package com.example.chatservice.domain.entiy;

import com.example.chatservice.domain.enums.MessageType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    private String id;

    private String messageUuId;
    private String chatRoomUuid;
    private String senderUuid;
    private String receiverUuid;

    private String content;
    private LocalDateTime sentAt;

    @Setter
    private boolean read;
    
    private MessageType messageType;
    
    // 시스템 이벤트용 추가 필드
    private String eventData; // JSON 형태로 이벤트 관련 데이터 저장


    @Builder
    public ChatMessage(
            String messageUuId, String chatRoomUuid, String senderUuid, String receiverUuid, String content,
            LocalDateTime sentAt, boolean read, MessageType messageType, String eventData
    ) {
        this.messageUuId = messageUuId;
        this.chatRoomUuid = chatRoomUuid;
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.content = content;
        this.sentAt = sentAt;
        this.read = read;
        this.messageType = messageType != null ? messageType : MessageType.CHAT;
        this.eventData = eventData;
    }
}
