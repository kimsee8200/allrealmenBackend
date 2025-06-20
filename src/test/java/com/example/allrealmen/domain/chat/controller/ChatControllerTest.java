package com.example.allrealmen.domain.chat.controller;

import com.example.allrealmen.domain.chat.dto.ChatMessageRequest;
import com.example.allrealmen.domain.chat.dto.ChatMessageResponse;
import com.example.allrealmen.domain.chat.dto.ChatRoomResponse;
import com.example.allrealmen.domain.chat.entity.ChatMessage;
import com.example.allrealmen.domain.chat.entity.ChatRoom;
import com.example.allrealmen.domain.chat.repository.ChatMessageRepository;
import com.example.allrealmen.domain.chat.repository.ChatRoomRepository;
import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    private String baseUrl;
    private Member customer;
    private Member admin;
    private ChatRoom chatRoom;
    private WebSocketStompClient stompClient;
    private final String WEBSOCKET_URI = "ws://localhost:{port}/ws-chat";
    private final String WEBSOCKET_TOPIC = "/topic/room.{roomId}";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/chat";
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();

        // 테스트용 고객 생성
        customer = new Member();
        customer.setId("customer123");
        customer.setRole(Member.Role.USER);
        memberRepository.save(customer);

        // 테스트용 관리자 생성
        admin = new Member();
        admin.setId("admin123");
        admin.setRole(Member.Role.ADMIN);
        memberRepository.save(admin);

        // 테스트용 채팅방 생성
        chatRoom = new ChatRoom();
        chatRoom.setCustomerId(customer.getId());
        chatRoom.setStatus(ChatRoom.ChatStatus.ACTIVE);
        chatRoom.setCreatedAt(LocalDateTime.now());
        chatRoom = chatRoomRepository.save(chatRoom);

        // WebSocket 클라이언트 설정
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("채팅방 목록 조회 API 테스트")
    void getChatRooms() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(admin.getId(), "password"); // 실제 환경에 맞는 인증 정보 사용

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/rooms?onlyActive=true",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(chatRoom.getId());
    }

    @Test
    @DisplayName("채팅방 메시지 이력 조회 API 테스트")
    void getChatMessages() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(admin.getId(), "password");

        // 테스트용 메시지 생성
        ChatMessage message = new ChatMessage();
        message.setRoomId(chatRoom.getId());
        message.setSenderId(customer.getId());
        message.setContent("테스트 메시지");
        message.setType(ChatMessage.MessageType.CHAT);
        message.setPlatform(ChatMessage.Platform.WEB);
        message.setSentAt(LocalDateTime.now());
        chatMessageRepository.save(message);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/rooms/" + chatRoom.getId() + "/messages",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(message.getContent());
    }

    @Test
    @DisplayName("상담방 생성 API 테스트")
    void createConsultationRoom() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(customer.getId(), "password");

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/consultation?applicationId=app123&customerId=" + customer.getId(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("상담방이 생성되었습니다.");
    }

    @Test
    @DisplayName("WebSocket을 통한 실시간 채팅 테스트")
    void testWebSocketChat() throws Exception {
        // given
        String url = WEBSOCKET_URI.replace("{port}", String.valueOf(port));
        CompletableFuture<ChatMessageResponse> completableFuture = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new TestStompSessionHandler();
        StompSession stompSession = stompClient.connectAsync(url, new WebSocketHttpHeaders(), sessionHandler)
                .get(1, TimeUnit.SECONDS);

        stompSession.subscribe(WEBSOCKET_TOPIC.replace("{roomId}", chatRoom.getId()),
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessageResponse.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        completableFuture.complete((ChatMessageResponse) payload);
                    }
                });

        // when
        ChatMessageRequest chatMessage = new ChatMessageRequest();
        chatMessage.setRoomId(chatRoom.getId());
        chatMessage.setSenderId(customer.getId());
        chatMessage.setContent("WebSocket 테스트 메시지");
        chatMessage.setType(ChatMessage.MessageType.CHAT);

        stompSession.send("/app/chat.send", chatMessage);

        // then
        ChatMessageResponse receivedMessage = completableFuture.get(3, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo(chatMessage.getContent());
    }

    private class TestStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void handleException(StompSession session, StompCommand command,
                                  StompHeaders headers, byte[] payload, Throwable exception) {
            exception.printStackTrace();
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            exception.printStackTrace();
        }
    }
} 