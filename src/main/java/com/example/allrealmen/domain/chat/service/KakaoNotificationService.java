package com.example.allrealmen.domain.chat.service;

import com.example.allrealmen.domain.chat.dto.ChatMessageResponse;
import com.example.allrealmen.domain.user.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoNotificationService {

    private final RestTemplate restTemplate;

    @Value("${kakao.api.admin-key}")
    private String adminKey;

    @Value("${kakao.api.template-id}")
    private String templateId;

    public void sendChatNotification(Member admin, ChatMessageResponse message) {
        String url = "https://kapi.kakao.com/v2/api/talk/memo/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        Map<String, Object> templateArgs = new HashMap<>();
        templateArgs.put("roomId", message.getRoomId());
        templateArgs.put("sender", message.getSenderId());
        templateArgs.put("message", message.getContent());

        Map<String, Object> body = new HashMap<>();
        body.put("template_id", templateId);
        body.put("template_args", templateArgs);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            // 카카오톡 알림 전송 실패 시 로그만 남기고 계속 진행
            // 실제 운영 환경에서는 적절한 에러 처리 및 재시도 로직 추가 필요
            e.printStackTrace();
        }
    }
} 