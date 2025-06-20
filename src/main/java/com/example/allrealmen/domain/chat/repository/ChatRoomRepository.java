package com.example.allrealmen.domain.chat.repository;

import com.example.allrealmen.domain.chat.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByStatus(ChatRoom.ChatStatus status);
} 