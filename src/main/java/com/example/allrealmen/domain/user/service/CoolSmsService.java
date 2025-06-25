package com.example.allrealmen.domain.user.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.Random;

@Slf4j
@Service
public class CoolSmsService {
    @Value("${naver-cloud.sms.access-key}")
    private String apiKey;

    @Value("${naver-cloud.sms.secret-key}")
    private String apiSecret;

    @Value("${naver-cloud.sms.sender-phone}")
    private String fromPhoneNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        // 메시지 서비스 초기화
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
    }

    public String sendVerificationSms(String to) {
        try {
            // 랜덤한 6자리 인증번호 생성
            String verificationCode = generateRandomCode();

            Message message = new Message();
            message.setFrom(fromPhoneNumber);
            message.setTo(to);
            message.setText("[매진남] 인증번호는 [" + verificationCode + "] 입니다.");

            // 메시지 전송
            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("SMS sent successfully. Message ID: {}", response.getMessageId());

            return verificationCode;
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
            throw new RuntimeException("SMS 전송에 실패했습니다: " + e.getMessage());
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 