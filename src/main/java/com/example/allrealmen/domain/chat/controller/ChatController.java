package com.example.allrealmen.domain.chat.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.chat.dto.ChatMessageRequest;
import com.example.allrealmen.domain.chat.dto.ChatMessageResponse;
import com.example.allrealmen.domain.chat.dto.ChatRoomResponse;
import com.example.allrealmen.domain.chat.entity.ChatMessage;
import com.example.allrealmen.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest chatMessage) {
        ChatMessageResponse response = chatService.sendMessage(chatMessage);
        
        // 웹소켓으로 메시지 전송
        messagingTemplate.convertAndSend(
            "/topic/room." + chatMessage.getRoomId(), 
            response
        );
        
        // 카카오톡으로 알림 전송 (관리자가 아닌 경우)
        if (!response.isFromAdmin()) {
            chatService.sendKakaoNotification(response);
        }
    }

    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessageRequest chatMessage, 
                        SimpMessageHeaderAccessor headerAccessor) {
        // 웹소켓 세션에 사용자 정보 저장
        headerAccessor.getSessionAttributes().put("room_id", chatMessage.getRoomId());
        headerAccessor.getSessionAttributes().put("user_id", chatMessage.getSenderId());
        
        // 입장 메시지 전송
        ChatMessageResponse response = chatService.joinRoom(chatMessage);
        messagingTemplate.convertAndSend(
            "/topic/room." + chatMessage.getRoomId(), 
            response
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getChatRooms(
            @RequestParam(defaultValue = "false") boolean onlyActive) {
        List<ChatRoomResponse> rooms = chatService.getChatRooms(onlyActive);
        return ResponseEntity.ok(new ApiResponse<>("200 OK", rooms, null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @PathVariable String roomId) {
        List<ChatMessageResponse> messages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(new ApiResponse<>("200 OK", messages, null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/{roomId}/close")
    public ResponseEntity<ApiResponse<Void>> closeRoom(@PathVariable String roomId) {
        chatService.closeRoom(roomId);
        return ResponseEntity.ok(new ApiResponse<>("200 OK", null, "채팅방이 종료되었습니다."));
    }

    @PostMapping("/consultation")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createConsultationRoom(
            @RequestParam String applicationId,
            @RequestParam String customerId) {
        ChatRoomResponse room = chatService.createConsultationRoom(customerId, applicationId);
        return ResponseEntity.ok(new ApiResponse<>("200 OK", room, "상담방이 생성되었습니다."));
    }
} 