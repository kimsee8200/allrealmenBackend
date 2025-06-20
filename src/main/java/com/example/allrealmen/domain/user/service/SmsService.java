package com.example.allrealmen.domain.user.service;

import com.example.allrealmen.config.NaverCloudConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final VerificationCodeStorage verificationCodeStorage;
    private final NaverCloudConfig naverCloudConfig;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    public void sendVerificationSms(String phoneNumber) {
        String verificationCode = generateVerificationCode();
        verificationCodeStorage.saveVerificationCode(phoneNumber, verificationCode);

        try {
            sendSms(phoneNumber, String.format("[AllRealMen] 인증번호 [%s]를 입력해주세요.", verificationCode));
        } catch (Exception e) {
            log.error("SMS 전송 실패: {}", e.getMessage());
            throw new RuntimeException("SMS 전송에 실패했습니다.", e);
        }
        
        // 만료된 코드 정리
        verificationCodeStorage.removeExpiredCodes();
    }

    public boolean verifySms(String phoneNumber, String code) {
        return verificationCodeStorage.verifyCode(phoneNumber, code);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendSms(String to, String content) throws IOException {
        long timestamp = System.currentTimeMillis();
        String signature = generateSignature(timestamp);

        // Request body 생성
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setType("SMS");
        messageRequest.setFrom(naverCloudConfig.getSenderPhone());
        messageRequest.setContent(content);
        messageRequest.setMessages(List.of(new Message(to)));

        String url = String.format("https://sens.apigw.ntruss.com/sms/v2/services/%s/messages",
                naverCloudConfig.getServiceId());

        RequestBody requestBody = RequestBody.create(
            MediaType.parse("application/json"),
            objectMapper.writeValueAsString(messageRequest)
        );

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-ncp-apigw-timestamp", String.valueOf(timestamp))
            .addHeader("x-ncp-iam-access-key", naverCloudConfig.getAccessKey())
            .addHeader("x-ncp-apigw-signature-v2", signature)
            .post(requestBody)
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("SMS 전송 실패: " + response.body().string());
            }
        }
    }

    private String generateSignature(long timestamp) {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = String.format("/sms/v2/services/%s/messages", naverCloudConfig.getServiceId());

        String message = new StringBuilder()
            .append(method)
            .append(space)
            .append(url)
            .append(newLine)
            .append(timestamp)
            .append(newLine)
            .append(naverCloudConfig.getAccessKey())
            .toString();

        try {
            SecretKeySpec signingKey = new SecretKeySpec(
                naverCloudConfig.getSecretKey().getBytes("UTF-8"),
                "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private static class MessageRequest {
        private String type;
        private String from;
        private String content;
        private List<Message> messages;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }
    }

    private static class Message {
        private String to;

        public Message(String to) {
            this.to = to;
        }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
    }
} 