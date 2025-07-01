package com.example.chatservice.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    
    // 일반 채팅 메시지
    CHAT("CHAT", "일반 채팅 메시지"),
    
    // 시스템 이벤트 메시지
    USER_JOINED("USER_JOINED", "사용자 입장"),
    USER_LEFT("USER_LEFT", "사용자 퇴장"),
    USER_CONNECTED("USER_CONNECTED", "사용자 연결"),
    USER_DISCONNECTED("USER_DISCONNECTED", "사용자 연결 해제"),
    
    // 채팅방 관련 이벤트
    ROOM_CREATED("ROOM_CREATED", "채팅방 생성"),
    ROOM_DELETED("ROOM_DELETED", "채팅방 삭제"),
    
    // 메시지 상태 이벤트
    MESSAGE_READ("MESSAGE_READ", "메시지 읽음"),
    MESSAGE_DELIVERED("MESSAGE_DELIVERED", "메시지 전달됨"),
    MESSAGE_FAILED("MESSAGE_FAILED", "메시지 전송 실패"),
    
    // 시스템 알림
    SYSTEM_NOTICE("SYSTEM_NOTICE", "시스템 공지"),
    MAINTENANCE_NOTICE("MAINTENANCE_NOTICE", "점검 공지");
    
    private final String code;
    private final String description;
    
    /**
     * 시스템 이벤트인지 확인
     */
    public boolean isSystemEvent() {
        return this != CHAT;
    }
    
    /**
     * 사용자 액션 이벤트인지 확인
     */
    public boolean isUserAction() {
        return this == USER_JOINED || this == USER_LEFT || this == MESSAGE_READ;
    }
    
    /**
     * 연결 상태 이벤트인지 확인
     */
    public boolean isConnectionEvent() {
        return this == USER_CONNECTED || this == USER_DISCONNECTED;
    }
    
    /**
     * 코드로 MessageType 찾기
     */
    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + code);
    }
} 