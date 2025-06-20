package com.example.allrealmen.domain.chat.dto;

import com.example.allrealmen.domain.chat.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private String id;
    private String roomId;
    private String senderId;
    private String content;
    private ChatMessage.MessageType type;
    private ChatMessage.Platform platform;
    private LocalDateTime sentAt;
    private boolean isRead;
    private boolean isFromAdmin;

    public static ChatMessageResponse from(ChatMessage message, boolean isFromAdmin) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .type(message.getType())
                .platform(message.getPlatform())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .isFromAdmin(isFromAdmin)
                .build();
    }
} 