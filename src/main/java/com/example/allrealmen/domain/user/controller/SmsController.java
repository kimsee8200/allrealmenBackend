package com.example.allrealmen.domain.user.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.user.dto.VerifySmsRequest;
import com.example.allrealmen.domain.user.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @GetMapping("/sms-confirm")
    public ResponseEntity<ApiResponse<Void>> sendVerificationSms(@RequestParam String phoneNumber) {
        try {
            log.info("Sending verification SMS");
            smsService.sendVerificationSms(phoneNumber);
            return ResponseEntity.ok(new ApiResponse<>("success", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", null, e.getMessage()));
        }
    }

    @PostMapping("/sms-verify")
    public ResponseEntity<ApiResponse<Boolean>> verifySms(@RequestBody VerifySmsRequest verifySmsRequest) {
        boolean isVerified = smsService.verifySms(verifySmsRequest.getPhoneNumber(), verifySmsRequest.getCode());
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse<>("success", true, null));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", false, null));
        }
    }
}

