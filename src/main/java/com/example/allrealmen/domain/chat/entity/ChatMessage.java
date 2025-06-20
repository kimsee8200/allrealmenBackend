package com.example.allrealmen.domain.chat.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    
    private String roomId;        // 채팅방 ID
    private String senderId;      // 발신자 ID
    private String content;       // 메시지 내용
    private MessageType type;     // 메시지 타입
    private Platform platform;    // 메시지 플랫폼
    private LocalDateTime sentAt; // 발송 시간
    private boolean isRead;       // 읽음 여부
    
    public enum MessageType {
        CHAT,       // 일반 채팅
        SYSTEM,     // 시스템 메시지
        FILE,       // 파일
        IMAGE       // 이미지
    }
    
    public enum Platform {
        WEB,        // 웹소켓 채팅
        KAKAO       // 카카오톡
    }
} 