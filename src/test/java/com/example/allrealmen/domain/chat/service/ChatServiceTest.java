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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoNotificationService kakaoNotificationService;

    private Member customer;
    private Member admin;
    private ChatRoom chatRoom;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        // 테스트용 고객 생성
        customer = new Member();
        customer.setId("customer123");
        customer.setRole(Member.Role.USER);

        // 테스트용 관리자 생성
        admin = new Member();
        admin.setId("admin123");
        admin.setRole(Member.Role.ADMIN);

        // 테스트용 채팅방 생성
        chatRoom = new ChatRoom();
        chatRoom.setId("room123");
        chatRoom.setCustomerId(customer.getId());
        chatRoom.setStatus(ChatRoom.ChatStatus.ACTIVE);
        chatRoom.setCreatedAt(LocalDateTime.now());

        // 테스트용 채팅 메시지 생성
        chatMessage = new ChatMessage();
        chatMessage.setId("message123");
        chatMessage.setRoomId(chatRoom.getId());
        chatMessage.setSenderId(customer.getId());
        chatMessage.setContent("테스트 메시지");
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        chatMessage.setPlatform(ChatMessage.Platform.WEB);
        chatMessage.setSentAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("메시지 전송 테스트")
    void sendMessage() {
        // given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setRoomId(chatRoom.getId());
        request.setSenderId(customer.getId());
        request.setContent("안녕하세요");
        request.setType(ChatMessage.MessageType.CHAT);

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        // when
        ChatMessageResponse response = chatService.sendMessage(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomId()).isEqualTo(chatRoom.getId());
        assertThat(response.getSenderId()).isEqualTo(customer.getId());
        assertThat(response.isFromAdmin()).isFalse();
        
        verify(chatRoomRepository).findById(chatRoom.getId());
        verify(memberRepository).findById(customer.getId());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("상담방 생성 테스트")
    void createConsultationRoom() {
        // given
        String applicationId = "app123";
        when(memberRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        // when
        ChatRoomResponse response = chatService.createConsultationRoom(customer.getId(), applicationId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());
        assertThat(response.getStatus()).isEqualTo(ChatRoom.ChatStatus.WAITING);
        
        verify(memberRepository).findById(customer.getId());
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("관리자가 상담방 입장 테스트")
    void joinRoomAsAdmin() {
        // given
        chatRoom.setStatus(ChatRoom.ChatStatus.WAITING);
        ChatMessageRequest request = new ChatMessageRequest();
        request.setRoomId(chatRoom.getId());
        request.setSenderId(admin.getId());

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        // when
        ChatMessageResponse response = chatService.joinRoom(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isFromAdmin()).isTrue();
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoom.ChatStatus.ACTIVE);
        assertThat(chatRoom.getAdminId()).isEqualTo(admin.getId());
        
        verify(chatRoomRepository).findById(chatRoom.getId());
        verify(memberRepository).findById(admin.getId());
        verify(chatRoomRepository).save(chatRoom);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅방 목록 조회 테스트")
    void getChatRooms() {
        // given
        List<ChatRoom> rooms = Arrays.asList(chatRoom);
        when(chatRoomRepository.findByStatus(ChatRoom.ChatStatus.ACTIVE)).thenReturn(rooms);
        when(chatMessageRepository.countByRoomIdAndIsReadFalse(chatRoom.getId())).thenReturn(5);

        // when
        List<ChatRoomResponse> response = chatService.getChatRooms(true);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(chatRoom.getId());
        assertThat(response.get(0).getUnreadCount()).isEqualTo(5);
        
        verify(chatRoomRepository).findByStatus(ChatRoom.ChatStatus.ACTIVE);
        verify(chatMessageRepository).countByRoomIdAndIsReadFalse(chatRoom.getId());
    }

    @Test
    @DisplayName("채팅방 메시지 이력 조회 테스트")
    void getChatMessages() {
        // given
        List<ChatMessage> messages = Arrays.asList(chatMessage);
        when(chatMessageRepository.findByRoomIdOrderBySentAtAsc(chatRoom.getId())).thenReturn(messages);
        when(memberRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        // when
        List<ChatMessageResponse> response = chatService.getChatMessages(chatRoom.getId());

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(chatMessage.getId());
        assertThat(response.get(0).isFromAdmin()).isFalse();
        
        verify(chatMessageRepository).findByRoomIdOrderBySentAtAsc(chatRoom.getId());
        verify(memberRepository).findById(customer.getId());
    }

    @Test
    @DisplayName("채팅방 종료 테스트")
    void closeRoom() {
        // given
        chatRoom.setAdminId(admin.getId());
        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        // when
        chatService.closeRoom(chatRoom.getId());

        // then
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoom.ChatStatus.CLOSED);
        assertThat(chatRoom.getClosedAt()).isNotNull();
        
        verify(chatRoomRepository).findById(chatRoom.getId());
        verify(chatRoomRepository).save(chatRoom);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 메시지 전송 시 예외 발생")
    void sendMessageToNonExistentRoom() {
        // given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setRoomId("nonexistent");
        request.setSenderId(customer.getId());
        request.setContent("테스트 메시지");

        when(chatRoomRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.sendMessage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 채팅방입니다.");
    }

    @Test
    @DisplayName("관리자가 이미 배정된 채팅방 입장 시 예외 발생")
    void joinAlreadyAssignedRoom() {
        // given
        chatRoom.setStatus(ChatRoom.ChatStatus.ACTIVE);
        chatRoom.setAdminId("other_admin");
        ChatMessageRequest request = new ChatMessageRequest();
        request.setRoomId(chatRoom.getId());
        request.setSenderId(admin.getId());

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> chatService.joinRoom(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 다른 상담사가 배정된 방입니다.");
    }
} 