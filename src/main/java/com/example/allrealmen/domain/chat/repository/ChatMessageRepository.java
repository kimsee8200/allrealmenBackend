package com.example.allrealmen.domain.chat.repository;

import com.example.allrealmen.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(String roomId);
    int countByRoomIdAndIsReadFalse(String roomId);
    void deleteByRoomId(String roomId);
} 