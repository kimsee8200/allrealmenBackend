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
import com.example.allrealmen.domain.user.security.CustomUserDetails;
import com.example.allrealmen.domain.user.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {

    private static final Logger log = LoggerFactory.getLogger(ChatControllerTest.class);
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String baseUrl;
    private Member customer;
    private Member admin;
    private ChatRoom chatRoom;
    private WebSocketStompClient stompClient;
    private final String WEBSOCKET_URI = "ws://localhost:{port}/ws";
    private final String WEBSOCKET_TOPIC = "/topic/room.{roomId}";
    private final String CHAT_SEND_ENDPOINT = "/app/chat.send";
    private final String CHAT_JOIN_ENDPOINT = "/app/chat.join";
    private String customerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/chat";
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();

        // 테스트용 고객 생성
        customer = new Member();
        customer.setId("customer123");
        customer.setPhoneNumber("010-2222-3333");
        customer.setRole(Member.Role.USER);
        customer.setPassword("password123");
        memberRepository.save(customer);
        
        // 고객 토큰 생성
        CustomUserDetails customerDetails = new CustomUserDetails(customer);
        Authentication customerAuth = new UsernamePasswordAuthenticationToken(
            customerDetails, 
            null, 
            customerDetails.getAuthorities()
        );
        customerToken = jwtTokenProvider.createToken(customerAuth);

        // 테스트용 관리자 생성
        admin = new Member();
        admin.setId("admin123");
        admin.setRole(Member.Role.ADMIN);
        admin.setPassword("admin123");
        memberRepository.save(admin);
        
        // 관리자 토큰 생성
        CustomUserDetails adminDetails = new CustomUserDetails(admin);
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
            adminDetails, 
            null, 
            adminDetails.getAuthorities()
        );
        adminToken = jwtTokenProvider.createToken(adminAuth);

        // 테스트용 채팅방 생성
        chatRoom = new ChatRoom();
        chatRoom.setCustomerId(customer.getId());
        chatRoom.setStatus(ChatRoom.ChatStatus.ACTIVE);
        chatRoom.setCreatedAt(LocalDateTime.now());
        chatRoom = chatRoomRepository.save(chatRoom);

        // WebSocket 클라이언트 설정
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        messageConverter.setObjectMapper(objectMapper);
        this.stompClient.setMessageConverter(messageConverter);
    }

    @Test
    @DisplayName("채팅방 목록 조회 API 테스트")
    void getChatRooms() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);

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
        headers.add("Authorization", "Bearer " + adminToken);

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
        headers.add("Authorization", "Bearer " + customerToken);

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
        List<ChatMessageResponse> customerMessages = new ArrayList<>();
        List<ChatMessageResponse> adminMessages = new ArrayList<>();

        // 고객 WebSocket 연결
        WebSocketHttpHeaders customerHeaders = new WebSocketHttpHeaders();
        customerHeaders.add("Authorization", "Bearer " + customerToken);
        StompHeaders customerConnectHeaders = new StompHeaders();
        customerConnectHeaders.add("Authorization", "Bearer " + customerToken);

        StompSession customerSession = stompClient.connectAsync(
                url,
                customerHeaders,
                customerConnectHeaders,
                new TestStompSessionHandler()
        ).get(1, TimeUnit.SECONDS);

        // 관리자 WebSocket 연결
        WebSocketHttpHeaders adminHeaders = new WebSocketHttpHeaders();
        adminHeaders.add("Authorization", "Bearer " + adminToken);
        StompHeaders adminConnectHeaders = new StompHeaders();
        adminConnectHeaders.add("Authorization", "Bearer " + adminToken);

        StompSession adminSession = stompClient.connectAsync(
                url,
                adminHeaders,
                adminConnectHeaders,
                new TestStompSessionHandler()
        ).get(1, TimeUnit.SECONDS);

        // 채팅방 구독 (고객)
        String roomTopic = WEBSOCKET_TOPIC.replace("{roomId}", chatRoom.getId());
        customerSession.subscribe(roomTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                customerMessages.add((ChatMessageResponse) payload);
            }
        });

        // 채팅방 구독 (관리자)
        adminSession.subscribe(roomTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                adminMessages.add((ChatMessageResponse) payload);
            }
        });

        // 고객이 채팅방 입장
        ChatMessageRequest customerJoinMessage = new ChatMessageRequest();
        customerJoinMessage.setRoomId(chatRoom.getId());
        customerJoinMessage.setSenderId(customer.getId());
        customerJoinMessage.setType(ChatMessage.MessageType.SYSTEM);
        customerJoinMessage.setContent(customer.getId() + "님이 입장하셨습니다.");
        customerSession.send(CHAT_JOIN_ENDPOINT, customerJoinMessage);

        // 고객이 메시지 전송
        ChatMessageRequest customerChatMessage = new ChatMessageRequest();
        customerChatMessage.setRoomId(chatRoom.getId());
        customerChatMessage.setSenderId(customer.getId());
        customerChatMessage.setContent("안녕하세요, 문의드립니다.");
        customerChatMessage.setType(ChatMessage.MessageType.CHAT);
        customerSession.send(CHAT_SEND_ENDPOINT, customerChatMessage);

        // 잠시 대기하여 메시지 전달 확인
        Thread.sleep(1000);

        // 관리자가 응답 메시지 전송
        ChatMessageRequest adminChatMessage = new ChatMessageRequest();
        adminChatMessage.setRoomId(chatRoom.getId());
        adminChatMessage.setSenderId(admin.getId());
        adminChatMessage.setContent("네, 무엇을 도와드릴까요?");
        adminChatMessage.setType(ChatMessage.MessageType.CHAT);
        adminSession.send(CHAT_SEND_ENDPOINT, adminChatMessage);

        // 잠시 대기하여 메시지 전달 확인
        Thread.sleep(1000);

        // then
        // 고객 측 메시지 확인
        assertThat(customerMessages).hasSize(3); // 시스템 메시지 + 관리자 응답
        log.info(customerMessages.toString());
        ChatMessageResponse lastCustomerReceived = customerMessages.get(2);
        assertThat(lastCustomerReceived.getSenderId()).isEqualTo(admin.getId());
        assertThat(lastCustomerReceived.getContent()).isEqualTo("네, 무엇을 도와드릴까요?");

        // 관리자 측 메시지 확인
        assertThat(adminMessages).hasSize(3); // 시스템 메시지 + 고객 문의
        ChatMessageResponse lastAdminReceived = adminMessages.get(0);
        log.info(adminMessages.get(0).getContent());
        assertThat(lastAdminReceived.getSenderId()).isEqualTo(customer.getId());
        assertThat(lastAdminReceived.getContent()).isEqualTo("안녕하세요, 문의드립니다.");

        // 연결 종료
        customerSession.disconnect();
        adminSession.disconnect();
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