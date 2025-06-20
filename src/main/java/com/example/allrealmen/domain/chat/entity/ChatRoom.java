package com.example.allrealmen.domain.chat.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "chat_rooms")
public class ChatRoom {
    @Id
    private String id;
    
    private String applicationId;  // 상담 신청 ID
    private String customerId;     // 고객 ID
    private String adminId;        // 상담 관리자 ID
    private ChatStatus status;     // 채팅방 상태
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    
    public enum ChatStatus {
        WAITING,    // 상담 대기중
        ACTIVE,     // 상담 진행중
        CLOSED      // 상담 종료
    }
} 