package com.example.allrealmen.domain.chat.service;

import com.example.allrealmen.domain.chat.dto.ChatMessageResponse;
import com.example.allrealmen.domain.chat.entity.ChatMessage;
import com.example.allrealmen.domain.user.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KakaoNotificationServiceTest {

    @InjectMocks
    private KakaoNotificationService kakaoNotificationService;

    @Mock
    private RestTemplate restTemplate;

    private Member admin;
    private ChatMessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 관리자 설정
        admin = new Member();
        admin.setId("admin123");
        admin.setRole(Member.Role.ADMIN);

        // 테스트용 메시지 응답 설정
        messageResponse = ChatMessageResponse.builder()
                .id("message123")
                .roomId("room123")
                .senderId("customer123")
                .content("테스트 메시지")
                .type(ChatMessage.MessageType.CHAT)
                .platform(ChatMessage.Platform.WEB)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .isFromAdmin(false)
                .build();

        // KakaoNotificationService의 private 필드 설정
        ReflectionTestUtils.setField(kakaoNotificationService, "adminKey", "test-admin-key");
        ReflectionTestUtils.setField(kakaoNotificationService, "templateId", "test-template-id");
    }

    @Test
    @DisplayName("카카오톡 알림 전송 테스트")
    void sendChatNotification() {
        // given
        String expectedUrl = "https://kapi.kakao.com/v2/api/talk/memo/send";
        when(restTemplate.postForObject(eq(expectedUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn("success");

        // HTTP 요청 캡처를 위한 ArgumentCaptor 설정
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = 
                ArgumentCaptor.forClass(HttpEntity.class);

        // when
        kakaoNotificationService.sendChatNotification(admin, messageResponse);

        // then
        verify(restTemplate).postForObject(
                eq(expectedUrl),
                requestCaptor.capture(),
                eq(String.class)
        );

        // HTTP 요청 검증
        HttpEntity<Map<String, Object>> capturedRequest = requestCaptor.getValue();
        
        // 헤더 검증
        HttpHeaders headers = capturedRequest.getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
        assertThat(headers.getFirst("Authorization")).isEqualTo("KakaoAK test-admin-key");

        // 바디 검증
        @SuppressWarnings("unchecked")
        Map<String, Object> body = capturedRequest.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("template_id")).isEqualTo("test-template-id");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> templateArgs = (Map<String, Object>) body.get("template_args");
        assertThat(templateArgs).isNotNull();
        assertThat(templateArgs.get("roomId")).isEqualTo(messageResponse.getRoomId());
        assertThat(templateArgs.get("sender")).isEqualTo(messageResponse.getSenderId());
        assertThat(templateArgs.get("message")).isEqualTo(messageResponse.getContent());
    }

    @Test
    @DisplayName("카카오톡 알림 전송 실패 시 예외 처리 테스트")
    void sendChatNotificationWithError() {
        // given
        String expectedUrl = "https://kapi.kakao.com/v2/api/talk/memo/send";
        when(restTemplate.postForObject(eq(expectedUrl), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // when
        kakaoNotificationService.sendChatNotification(admin, messageResponse);

        // then
        verify(restTemplate).postForObject(
                eq(expectedUrl),
                any(HttpEntity.class),
                eq(String.class)
        );
        // 예외가 발생해도 서비스는 계속 실행되어야 함
    }
} 