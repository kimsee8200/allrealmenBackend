package com.example.allrealmen.domain.chat.dto;

import com.example.allrealmen.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {
    private String id;
    private String applicationId;
    private String customerId;
    private String adminId;
    private ChatRoom.ChatStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private int unreadCount;  // 읽지 않은 메시지 수

    public static ChatRoomResponse from(ChatRoom room, int unreadCount) {
        return ChatRoomResponse.builder()
                .id(room.getId())
                .applicationId(room.getApplicationId())
                .customerId(room.getCustomerId())
                .adminId(room.getAdminId())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .closedAt(room.getClosedAt())
                .unreadCount(unreadCount)
                .build();
    }
} 