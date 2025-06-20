package com.example.allrealmen.domain.user.service;

import com.example.allrealmen.domain.user.entity.SmsVerification;
import com.example.allrealmen.domain.user.repository.SmsVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeStorage {
    private static final long EXPIRATION_MINUTES = 5;
    private final SmsVerificationRepository repository;

    @Transactional
    public void saveVerificationCode(String phoneNumber, String code) {
        // 기존 코드가 있다면 삭제
        repository.findFirstByPhoneNumberOrderByExpirationTimeDesc(phoneNumber)
                .ifPresent(verification -> repository.delete(verification));

        // 새로운 인증 코드 저장
        SmsVerification verification = new SmsVerification(
            phoneNumber, 
            code, 
            LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)
        );
        repository.save(verification);
    }

    @Transactional(readOnly = true)
    public boolean verifyCode(String phoneNumber, String code) {
        return repository.findFirstByPhoneNumberOrderByExpirationTimeDesc(phoneNumber)
                .map(verification -> {
                    if (verification.getCode().equals(code) && !verification.isExpired()) {
                        repository.delete(verification);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    @Transactional
    public void removeExpiredCodes() {
        repository.deleteExpiredCodes(LocalDateTime.now());
    }
} 