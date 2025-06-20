package com.example.allrealmen.domain.chat.dto;

import com.example.allrealmen.domain.chat.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    @NotBlank(message = "채팅방 ID는 필수입니다.")
    private String roomId;
    
    @NotBlank(message = "발신자 ID는 필수입니다.")
    private String senderId;
    
    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
    
    private ChatMessage.MessageType type = ChatMessage.MessageType.CHAT;
} 