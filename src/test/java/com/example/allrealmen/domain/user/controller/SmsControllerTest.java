package com.example.allrealmen.domain.user.controller;

import com.example.allrealmen.domain.user.dto.VerifySmsRequest;
import com.example.allrealmen.domain.user.service.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmsController.class)
class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmsService smsService;

    private String phoneNumber;
    private String verificationCode;
    private VerifySmsRequest verifySmsRequest;

    @BeforeEach
    void setUp() {
        phoneNumber = "01012345678";
        verificationCode = "123456";
        verifySmsRequest = new VerifySmsRequest();
        verifySmsRequest.setPhoneNumber(phoneNumber);
        verifySmsRequest.setCode(verificationCode);
    }

    @Test
    @DisplayName("SMS 인증번호 전송 성공")
    void sendVerificationSmsSuccess() throws Exception {
        // given
        doNothing().when(smsService).sendVerificationSms(any());

        // when & then
        mockMvc.perform(get("/api/auth/sms-confirm")
                .param("phoneNumber", phoneNumber))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("SMS 인증번호 검증 성공")
    void verifySmsSuccess() throws Exception {
        // given
        when(smsService.verifySms(any(), any())).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/sms-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifySmsRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("SMS 인증번호 검증 실패")
    void verifySmsFailure() throws Exception {
        // given
        when(smsService.verifySms(any(), any())).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/auth/sms-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifySmsRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.data").value(false));
    }
} 