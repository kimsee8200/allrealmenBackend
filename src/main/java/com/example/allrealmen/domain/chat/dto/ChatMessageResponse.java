package com.example.allrealmen.domain.chat.dto;

import com.example.allrealmen.domain.chat.entity.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String id;
    private String roomId;
    private String senderId;
    private String content;
    private ChatMessage.MessageType type;
    private ChatMessage.Platform platform;
    private LocalDateTime sentAt;
    
    @JsonProperty("read")
    private boolean isRead;
    
    @JsonProperty("fromAdmin")
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