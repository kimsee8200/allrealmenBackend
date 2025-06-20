package com.example.allrealmen.domain.user.service;

import com.example.allrealmen.config.NaverCloudConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @InjectMocks
    private SmsService smsService;

    @Mock
    private VerificationCodeStorage verificationCodeStorage;

    @Mock
    private NaverCloudConfig naverCloudConfig;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OkHttpClient okHttpClient;

    private String phoneNumber;
    private String verificationCode;

    @BeforeEach
    void setUp() {
        phoneNumber = "01012345678";
        verificationCode = "123456";
    }

    @Test
    @DisplayName("SMS 인증번호 전송 성공")
    void sendVerificationSmsSuccess() {
        // given
        doNothing().when(verificationCodeStorage).saveVerificationCode(any(), any());

        // when
        smsService.sendVerificationSms(phoneNumber);

        // then
        verify(verificationCodeStorage).saveVerificationCode(eq(phoneNumber), any());
    }

    @Test
    @DisplayName("SMS 인증번호 검증 성공")
    void verifySmsSuccess() {
        // given
        when(verificationCodeStorage.verifyCode(phoneNumber, verificationCode)).thenReturn(true);

        // when
        boolean result = smsService.verifySms(phoneNumber, verificationCode);

        // then
        assertThat(result).isTrue();
        verify(verificationCodeStorage).verifyCode(phoneNumber, verificationCode);
    }

    @Test
    @DisplayName("SMS 인증번호 검증 실패")
    void verifySmsFailure() {
        // given
        when(verificationCodeStorage.verifyCode(phoneNumber, verificationCode)).thenReturn(false);

        // when
        boolean result = smsService.verifySms(phoneNumber, verificationCode);

        // then
        assertThat(result).isFalse();
        verify(verificationCodeStorage).verifyCode(phoneNumber, verificationCode);
    }
} 