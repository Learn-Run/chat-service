package com.example.chatservice.common.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        log.info(">>> WebSocket Handshake 시작: {}", request.getRemoteAddress());
        
        // 사용자 UUID 검증 (실제로는 JWT 토큰 검증 필요)
        String userUuid = extractUserUuid(request);
        if (userUuid == null) {
            log.warn("사용자 UUID가 없어 연결을 거부합니다.");
            return false;
        }
        
        // attributes에 사용자 정보 저장
        attributes.put("userUuid", userUuid);
        log.info("사용자 인증 성공: userUuid={}", userUuid);
        
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            log.error("WebSocket Handshake 실패: {}", exception.getMessage());
        } else {
            log.info(">>> WebSocket Handshake 완료");
        }
    }
    
    /**
     * 요청에서 사용자 UUID 추출
     * 실제 구현에서는 JWT 토큰을 파싱하여 사용자 정보를 추출해야 함
     */
    private String extractUserUuid(ServerHttpRequest request) {
        // 1. 헤더에서 UUID 추출 시도
        String userUuid = request.getHeaders().getFirst("X-Member-UUID");
        if (userUuid != null && !userUuid.trim().isEmpty()) {
            return userUuid.trim();
        }
        
        // 2. 쿼리 파라미터에서 UUID 추출 시도
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userUuid=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userUuid=")) {
                    return param.substring("userUuid=".length());
                }
            }
        }
        
        return null;
    }
}
