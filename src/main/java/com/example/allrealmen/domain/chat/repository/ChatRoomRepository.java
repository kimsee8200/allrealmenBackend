package com.example.allrealmen.domain.chat.repository;

import com.example.allrealmen.domain.chat.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByStatus(ChatRoom.ChatStatus status);
    Optional<ChatRoom> findByCustomerIdAndStatusNot(String customerId, ChatRoom.ChatStatus status);
    List<ChatRoom> findByStatusAndClosedAtBefore(ChatRoom.ChatStatus status, LocalDateTime closedAt);
} 