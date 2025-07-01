package com.example.chatservice.domain.presentation;

import com.example.chatservice.common.entity.BaseResponseEntity;
import com.example.chatservice.common.response.CursorPage;
import com.example.chatservice.domain.application.ChatRoomService;
import com.example.chatservice.domain.dto.in.ChatMessageReqDto;
import com.example.chatservice.domain.dto.in.ChatRoomListReqDto;
import com.example.chatservice.domain.dto.in.CreateChatRoomReqDto;
import com.example.chatservice.domain.dto.in.MarkMessageAsReadReqDto;
import com.example.chatservice.domain.dto.out.ChatRoomListResDto;
import com.example.chatservice.domain.dto.out.ChatMessageResDto;
import com.example.chatservice.domain.vo.in.CreateChatRoomReqVo;
import com.example.chatservice.domain.vo.out.CreateChatRoomResVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat-room")
@Tag(name = "ChatRoom", description = "채팅방 관련 API")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(
            summary = "채팅방 생성",
            description = """
                        두 사용자의 UUID를 기반으로 채팅방을 생성하거나 기존 채팅방을 반환합니다.
                    
                        [요청 경로]
                        - POST /api/v1/chat-room/create
                    
                        [요청 헤더]
                        - X-Member-UUID: (String) 채팅 참여자 A의 UUID
                    
                        [요청 바디]
                        - participantBUuid: (String) 채팅 참여자 B의 UUID
                    
                        [응답 필드]
                        - chatRoomUuid: (String) 생성된 채팅방 UUID
                    
                        [처리 로직]
                        - 두 참여자 간 기존 채팅방 존재 여부 확인 후 없으면 새로 생성
                    
                        [예외 상황]
                        - INVALID_MEMBER_UUID: 잘못된 사용자 UUID 형식
                    """
    )
    @PostMapping("/create")
    public BaseResponseEntity<CreateChatRoomResVo> createChatRoom(
            @RequestHeader("X-Member-UUID") String participantAUuid,
            @RequestBody CreateChatRoomReqVo vo
    ) {

        CreateChatRoomReqDto dto = CreateChatRoomReqDto.of(participantAUuid, vo.getParticipantBUuid());
        return new BaseResponseEntity<>(chatRoomService.createOrGetRoom(dto).toVo());
    }

    @Operation(
            summary = "채팅방 읽음 처리",
            description = """
                        사용자가 채팅방의 안읽은 메시지를 모두 읽음 처리합니다.
                    
                        [요청 경로]
                        - PATCH /api/v1/chat-room/read/{chatRoomUuid}
                    
                        [요청 헤더]
                        - X-Member-UUID: (String) 읽은 사람의 UUID
                    
                        [요청 파라미터]
                        - path variable: chatRoomUuid (String) 채팅방 고유 ID
                    
                        [처리 로직]
                        - 해당 채팅방의 안 읽은 메시지를 읽음 처리
                    
                    """
    )
    @PatchMapping("/read/{chatRoomUuid}")
    public BaseResponseEntity<Void> readChatMessage(
            @RequestHeader("X-Member-UUID") String receiverUuid,
            @PathVariable String chatRoomUuid
    ) {
        chatRoomService.markUnreadMessagesAsRead(MarkMessageAsReadReqDto.of(receiverUuid, chatRoomUuid));
        return new BaseResponseEntity<>();
    }

    @Operation(
            summary = "채팅방 목록 커서 기반 조회",
            description = """
        회원이 참여 중인 채팅방을 커서 기반 페이지네이션 방식으로 조회합니다.

        [정렬 방식]
        - 채팅방은 마지막 메시지 시간(`lastMessageTime`) 기준으로 내림차순 정렬됩니다.
        - 단, `lastMessageTime`이 없는 채팅방은 생성일(`createdAt`) 기준으로 정렬됩니다.
        
        [요청 경로]
        - GET /api/v1/chat-room/list

        [요청 헤더]
        - X-Member-UUID: (String) 회원 고유 식별자

        [요청 파라미터 - QueryString]
        - cursor: (String) 이전 페이지 마지막 채팅방의 마지막 메시지 시간 (ISO 8601 형식, nullable)
        - size: (int) 페이지 크기 (기본값: 10)

        [요청 예시]
        - /api/v1/chat-room/list?cursor=2025-06-28T15:33:40

        [응답 필드]
        - content: 채팅방 리스트
        - nextCursor: 다음 페이지 조회를 위한 커서 (마지막 채팅방의 lastMessageTime)
        - hasNext: 다음 페이지 존재 여부
    """
    )
    @GetMapping("/list")
    public BaseResponseEntity<CursorPage<ChatRoomListResDto>> getChatRoomList(
            @RequestHeader("X-Member-UUID") String memberUuid,
            @ModelAttribute ChatRoomListReqDto chatRoomListReqDto
    ) {
        return new BaseResponseEntity<>(chatRoomService.getChatRoomList(memberUuid,chatRoomListReqDto));
    }

    @Operation(
            summary = "단일 채팅방 정보 조회",
            description = """
            특정 채팅방 UUID와 회원 UUID를 기반으로 해당 채팅방의 정보를 조회합니다.

            [요청 경로]
            - GET /api/v1/chat-room/{chatRoomUuid}

            [요청 헤더]
            - X-Member-UUID: (String) 조회 요청 회원 UUID

            [요청 파라미터]
            - path variable: chatRoomUuid (String) 채팅방 UUID

            [응답 필드]
            - 채팅방 상대 정보, 마지막 메시지, 안 읽은 메시지 수 등

        """
    )
    @GetMapping("/{chatRoomUuid}")
    public BaseResponseEntity<ChatRoomListResDto> getChatRoom(
            @PathVariable String chatRoomUuid,
            @RequestHeader("X-Member-UUID") String memberUuid
    ) {
        return new BaseResponseEntity<>(chatRoomService.getChatRoom(memberUuid,chatRoomUuid));
    }

    @Operation(
            summary = "채팅방 메시지 목록 조회",
            description = """
            특정 채팅방의 메시지를 커서 기반으로 페이징 조회합니다.

            [요청 경로]
            - GET /api/v1/chat-room/{chatRoomUuid}/message

            [요청 파라미터]
            - path variable: chatRoomUuid (String) 채팅방 UUID
            - cursor: (String) 마지막 메시지 UUID (nullable)
            - size: (int) 페이지 크기 (기본값: 10)

            [응답 필드]
            - content: 메시지 리스트
            - nextCursor: 다음 페이지 커서

            [예외 상황]
            - NO_EXIST_CHAT_ROOM: 채팅방 존재하지 않음
        """
    )
    @GetMapping("/{chatRoomUuid}/message")
    public BaseResponseEntity<CursorPage<ChatMessageResDto>> getChatMessages(
            @PathVariable String chatRoomUuid,
            @ModelAttribute ChatMessageReqDto chatMessageReqDto
    ) {
        return new BaseResponseEntity<>(chatRoomService.getChatMessages(chatRoomUuid, chatMessageReqDto));
    }

    @Operation(
            summary = "채팅방 연결 상태 조회",
            description = """
            특정 채팅방의 사용자 연결 상태를 조회합니다.

            [요청 경로]
            - GET /api/v1/chat-room/{chatRoomUuid}/status

            [요청 헤더]
            - X-Member-UUID: (String) 조회 요청 회원 UUID

            [응답 필드]
            - connectedUsers: 연결된 사용자 목록
            - totalParticipants: 전체 참여자 수
        """
    )
    @GetMapping("/{chatRoomUuid}/status")
    public BaseResponseEntity<Map<String, Object>> getChatRoomStatus(
            @PathVariable String chatRoomUuid,
            @RequestHeader("X-Member-UUID") String memberUuid
    ) {
        return new BaseResponseEntity<>(chatRoomService.getChatRoomStatus(chatRoomUuid, memberUuid));
    }
}
