package com.example.chatservice.common.config;

import com.example.chatservice.domain.application.ChatEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatEventService chatEventService;
    
    // 연결된 사용자 관리 (실제 운영에서는 Redis 사용 권장)
    private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();
    
    // 채팅방별 연결된 사용자 관리
    private final Map<String, Map<String, String>> chatRoomUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userUuid = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        
        if (userUuid != null) {
            connectedUsers.put(sessionId, userUuid);
            log.info("사용자 연결: sessionId={}, userUuid={}", sessionId, userUuid);
            
            // 연결 이벤트 저장 (모든 참여 채팅방에 대해)
            chatRoomUsers.forEach((chatRoomUuid, users) -> {
                if (users.containsValue(userUuid)) {
                    chatEventService.saveUserConnectedEvent(chatRoomUuid, userUuid);
                }
            });
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userUuid = connectedUsers.remove(sessionId);
        
        if (userUuid != null) {
            log.info("사용자 연결 해제: sessionId={}, userUuid={}", sessionId, userUuid);
            
            // 연결 해제 이벤트 저장 (모든 참여 채팅방에 대해)
            chatRoomUsers.forEach((chatRoomUuid, users) -> {
                if (users.containsValue(userUuid)) {
                    chatEventService.saveUserDisconnectedEvent(chatRoomUuid, userUuid);
                }
            });
            
            // 모든 채팅방에서 해당 사용자 제거
            chatRoomUsers.forEach((chatRoomUuid, users) -> {
                users.remove(sessionId);
                if (users.isEmpty()) {
                    chatRoomUsers.remove(chatRoomUuid);
                }
            });
            
            // 연결 해제 알림 (선택사항)
            notifyUserDisconnected(userUuid);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String userUuid = connectedUsers.get(sessionId);
        
        if (userUuid != null && destination != null && destination.startsWith("/queue/messages/")) {
            String chatRoomUuid = destination.replace("/queue/messages/", "");
            
            // 채팅방별 사용자 관리
            chatRoomUsers.computeIfAbsent(chatRoomUuid, k -> new ConcurrentHashMap<>())
                    .put(sessionId, userUuid);
            
            log.info("채팅방 구독: chatRoomUuid={}, userUuid={}, sessionId={}", 
                    chatRoomUuid, userUuid, sessionId);
            
            // 채팅방 입장 이벤트 저장
            chatEventService.saveUserJoinedEvent(chatRoomUuid, userUuid);
            
            // 채팅방 입장 알림
            notifyUserJoinedChatRoom(chatRoomUuid, userUuid);
        }
    }

    /**
     * 특정 사용자가 연결되어 있는지 확인
     */
    public boolean isUserConnected(String userUuid) {
        return connectedUsers.containsValue(userUuid);
    }

    /**
     * 특정 채팅방에 사용자가 연결되어 있는지 확인
     */
    public boolean isUserInChatRoom(String chatRoomUuid, String userUuid) {
        Map<String, String> users = chatRoomUsers.get(chatRoomUuid);
        return users != null && users.containsValue(userUuid);
    }

    /**
     * 특정 채팅방의 모든 연결된 사용자 UUID 목록 반환
     */
    public java.util.Set<String> getConnectedUsersInChatRoom(String chatRoomUuid) {
        Map<String, String> users = chatRoomUsers.get(chatRoomUuid);
        return users != null ? new java.util.HashSet<>(users.values()) : new java.util.HashSet<>();
    }

    /**
     * 사용자 연결 해제 알림
     */
    private void notifyUserDisconnected(String userUuid) {
        // 연결 해제된 사용자와 관련된 채팅방에 알림
        chatRoomUsers.forEach((chatRoomUuid, users) -> {
            if (users.containsValue(userUuid)) {
                Map<String, Object> disconnectNotification = Map.of(
                        "type", "USER_DISCONNECTED",
                        "userUuid", userUuid,
                        "timestamp", java.time.LocalDateTime.now()
                );
                
                messagingTemplate.convertAndSend("/queue/messages/" + chatRoomUuid, disconnectNotification);
            }
        });
    }

    /**
     * 채팅방 입장 알림
     */
    private void notifyUserJoinedChatRoom(String chatRoomUuid, String userUuid) {
        Map<String, Object> joinNotification = Map.of(
                "type", "USER_JOINED",
                "userUuid", userUuid,
                "timestamp", java.time.LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend("/queue/messages/" + chatRoomUuid, joinNotification);
    }
} 