package com.example.allrealmen.domain.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sms_verifications")
@Getter
@NoArgsConstructor
public class SmsVerification {
    @Id
    private String id;

    @Indexed
    private String phoneNumber;

    private String code;

    private LocalDateTime expirationTime;

    public SmsVerification(String phoneNumber, String code, LocalDateTime expirationTime) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.expirationTime = expirationTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
} 