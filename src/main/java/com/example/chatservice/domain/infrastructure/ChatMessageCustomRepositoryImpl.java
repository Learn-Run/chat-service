package com.example.chatservice.domain.infrastructure;

import com.example.chatservice.common.response.CursorPage;
import com.example.chatservice.domain.dto.in.ChatMessageReqDto;
import com.example.chatservice.domain.entiy.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository {

    private final MongoTemplate mongoTemplate;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public Map<String, Integer> getUnreadMessageCountByChatRoom(String receiverUuid) {
        MatchOperation match = match(
                Criteria.where("receiverUuid").is(receiverUuid).and("read").is(false)
        );

        GroupOperation group = group("chatRoomUuid").count().as("count");

        Aggregation aggregation = newAggregation(match, group);

        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, "chat_message", Document.class);

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("_id"),
                        doc -> doc.get("count", Number.class).intValue()
                ));
    }

    @Override
    public CursorPage<ChatMessage> findChatMessagesByCursor(String chatRoomUuid, ChatMessageReqDto chatMessageReqDto) {
        int size = chatMessageReqDto.getSize() != null ? chatMessageReqDto.getSize() : DEFAULT_PAGE_SIZE;
        LocalDateTime cursor = chatMessageReqDto.getCursor();
        Criteria criteria = Criteria.where("chatRoomUuid").is(chatRoomUuid);

        if (cursor != null) {
            criteria = criteria.and("sentAt").lt(chatMessageReqDto.getCursor());
        }

        // 메시지 타입 필터링
        if (chatMessageReqDto.getMessageTypes() != null && !chatMessageReqDto.getMessageTypes().isEmpty()) {
            criteria = criteria.and("messageType").in(chatMessageReqDto.getMessageTypes());
        }

        // 시스템 이벤트 포함 여부
        if (chatMessageReqDto.getIncludeSystemEvents() != null && !chatMessageReqDto.getIncludeSystemEvents()) {
            criteria = criteria.and("messageType").is("CHAT"); // 일반 채팅 메시지만
        }

        log.info("cursor: {}, messageTypes: {}, includeSystemEvents: {}", 
                cursor, chatMessageReqDto.getMessageTypes(), chatMessageReqDto.getIncludeSystemEvents());
        
        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "sentAt"))
                .limit(size + 1);  // hasNext 판별 위해 +1

        List<ChatMessage> messages = mongoTemplate.find(query, ChatMessage.class);
        boolean hasNext = messages.size() > size;

        if (hasNext) messages = messages.subList(0, size);

        String nextCursor = hasNext
                ? messages.get(messages.size() - 1).getSentAt().toString()
                : null;

        return CursorPage.of(messages, hasNext, nextCursor);
    }
}
