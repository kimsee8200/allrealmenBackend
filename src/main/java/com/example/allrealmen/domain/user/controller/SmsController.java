package com.example.allrealmen.domain.user.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.user.dto.SmsRequest;
import com.example.allrealmen.domain.user.dto.VerifySmsRequest;
import com.example.allrealmen.domain.user.repository.SmsVerificationRepository;
import com.example.allrealmen.domain.user.service.CoolSmsService;
import com.example.allrealmen.domain.user.service.SmsService;
import com.example.allrealmen.domain.user.service.VerificationCodeStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;
    private final CoolSmsService coolSmsService;
    private final VerificationCodeStorage verificationCodeStorage;

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<String>> sendVerificationSms(@RequestBody SmsRequest smsRequest) {
        try {
            String phoneNumber = smsRequest.getPhoneNumber();
            log.info("Sending verification SMS to {}", phoneNumber);
            String verificationCode = coolSmsService.sendVerificationSms(phoneNumber);
            verificationCodeStorage.saveVerificationCode(phoneNumber, verificationCode);
            return ResponseEntity.ok(new ApiResponse<>("success", verificationCode, null));
        } catch (Exception e) {
            log.error("Failed to send SMS", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", null, e.getMessage()));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Boolean>> verifySms(@RequestBody VerifySmsRequest verifySmsRequest) {
        boolean isVerified = smsService.verifySms(verifySmsRequest.getPhoneNumber(), verifySmsRequest.getCode());
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse<>("success", true, null));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", false, null));
        }
    }
}

