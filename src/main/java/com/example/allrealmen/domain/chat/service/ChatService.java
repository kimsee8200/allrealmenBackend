package com.example.allrealmen.domain.chat.service;

import com.example.allrealmen.domain.chat.dto.ChatMessageRequest;
import com.example.allrealmen.domain.chat.dto.ChatMessageResponse;
import com.example.allrealmen.domain.chat.dto.ChatRoomResponse;
import com.example.allrealmen.domain.chat.entity.ChatMessage;
import com.example.allrealmen.domain.chat.entity.ChatRoom;
import com.example.allrealmen.domain.chat.repository.ChatMessageRepository;
import com.example.allrealmen.domain.chat.repository.ChatRoomRepository;
import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final KakaoNotificationService kakaoNotificationService;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderId) {
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        ChatMessage message = new ChatMessage();
        message.setRoomId(request.getRoomId());
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setPlatform(ChatMessage.Platform.WEB);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);

        message = chatMessageRepository.save(message);
        
        boolean isFromAdmin = sender.getRole() == Member.Role.ADMIN;
        return ChatMessageResponse.from(message, isFromAdmin);
    }

    @Transactional
    public ChatRoomResponse createConsultationRoom(String customerId) {
        Member customer = memberRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (customer.getRole() == Member.Role.ADMIN) {
            throw new IllegalArgumentException("관리자는 상담을 신청할 수 없습니다.");
        }

        // 이미 존재하는 활성 채팅방 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCustomerIdAndStatusNot(
            customerId, ChatRoom.ChatStatus.CLOSED);
        if (existingRoom.isPresent()) {
            throw new IllegalStateException("이미 진행 중인 상담이 있습니다.");
        }

        ChatRoom room = new ChatRoom();
        room.setCustomerId(customerId);
        room.setStatus(ChatRoom.ChatStatus.WAITING);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        
        room = chatRoomRepository.save(room);

        // 시스템 메시지 저장
        ChatMessage message = new ChatMessage();
        message.setRoomId(room.getId());
        message.setSenderId(customerId);
        message.setContent("상담이 신청되었습니다. 상담사 배정을 기다려주세요.");
        message.setType(ChatMessage.MessageType.SYSTEM);
        message.setPlatform(ChatMessage.Platform.WEB);
        message.setSentAt(LocalDateTime.now());
        message.setRead(true);
        chatMessageRepository.save(message);

        return ChatRoomResponse.from(room, 0);
    }

    @Transactional
    public ChatMessageResponse joinRoom(ChatMessageRequest request, String senderId) {
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        Member member = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (member.getRole() == Member.Role.ADMIN) {
            // 관리자 입장 시 검증
            if (room.getStatus() != ChatRoom.ChatStatus.WAITING) {
                throw new IllegalStateException("대기중인 상담방만 입장 가능합니다.");
            }
            if (room.getAdminId() != null) {
                throw new IllegalStateException("이미 다른 상담사가 배정된 방입니다.");
            }
            
            room.setAdminId(member.getId());
            room.setStatus(ChatRoom.ChatStatus.ACTIVE);
            chatRoomRepository.save(room);
        } else {
            // 사용자 입장 시 검증
            if (!room.getCustomerId().equals(member.getId())) {
                throw new IllegalStateException("상담 신청자만 입장 가능합니다.");
            }
        }

        ChatMessage message = new ChatMessage();
        message.setRoomId(request.getRoomId());
        message.setSenderId(senderId);
        message.setContent(member.getRole() == Member.Role.ADMIN ? "상담사가 입장했습니다." : "고객이 입장했습니다.");
        message.setType(ChatMessage.MessageType.SYSTEM);
        message.setPlatform(ChatMessage.Platform.WEB);
        message.setSentAt(LocalDateTime.now());
        message.setRead(true);

        message = chatMessageRepository.save(message);
        
        boolean isFromAdmin = member.getRole() == Member.Role.ADMIN;
        return ChatMessageResponse.from(message, isFromAdmin);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRooms(boolean onlyActive) {
        List<ChatRoom> rooms;
        if (onlyActive) {
            rooms = chatRoomRepository.findByStatus(ChatRoom.ChatStatus.ACTIVE);
        } else {
            rooms = chatRoomRepository.findAll();
        }

        return rooms.stream()
                .map(room -> {
                    int unreadCount = chatMessageRepository.countByRoomIdAndIsReadFalse(room.getId());
                    return ChatRoomResponse.from(room, unreadCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(String roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
        
        return messages.stream()
                .map(message -> {
                    Member sender = memberRepository.findById(message.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                    boolean isFromAdmin = sender.getRole() == Member.Role.ADMIN;
                    return ChatMessageResponse.from(message, isFromAdmin);
                })
                .toList();
    }

    @Transactional
    public void closeRoom(String roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        room.setStatus(ChatRoom.ChatStatus.CLOSED);
        room.setClosedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // 채팅방 종료 메시지 저장
        ChatMessage message = new ChatMessage();
        message.setRoomId(roomId);
        message.setSenderId(room.getAdminId());
        message.setContent("상담이 종료되었습니다.");
        message.setType(ChatMessage.MessageType.SYSTEM);
        message.setPlatform(ChatMessage.Platform.WEB);
        message.setSentAt(LocalDateTime.now());
        message.setRead(true);
        chatMessageRepository.save(message);
    }

    @Transactional
    public void sendKakaoNotification(ChatMessageResponse message) {
        ChatRoom room = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (room.getStatus() == ChatRoom.ChatStatus.ACTIVE && room.getAdminId() != null) {
            Member admin = memberRepository.findById(room.getAdminId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
            
            kakaoNotificationService.sendChatNotification(admin, message);
        }
    }
} 