package com.example.allrealmen.domain.application.controller;

import com.example.allrealmen.domain.application.dto.ApplicationFileRequest;
import com.example.allrealmen.domain.application.dto.ApplicationListResponse;
import com.example.allrealmen.domain.application.dto.ApplicationResponse;
import com.example.allrealmen.domain.application.dto.CreateApplicationRequest;
import com.example.allrealmen.domain.application.entity.Application;
import com.example.allrealmen.domain.application.service.ApplicationService;
import com.example.allrealmen.domain.user.security.JwtAuthenticationFilter;
import com.example.allrealmen.domain.user.security.JwtTokenProvider;
import com.example.allrealmen.config.SecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import com.example.allrealmen.domain.user.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MemberRepository memberRepository;

    private ApplicationResponse applicationResponse;
    private ApplicationListResponse applicationListResponse;
    private CreateApplicationRequest createApplicationRequest;
    private ApplicationFileRequest applicationFileRequest;
    private MockMultipartFile acConditionPhoto;
    private MockMultipartFile acStickerPhoto;
    private MockMultipartFile requestFile;
    private Application application;
    private Member testUser;
    private String userToken;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setUpMockAuthentication() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void setUp() throws Exception {
        // 기존 데이터 정리
        memberRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = new Member();
        testUser.setId("testuser");
        testUser.setPhoneNumber("010-1234-5678");
        testUser.setRole(Member.Role.USER);
        testUser.setPassword("password");
        memberRepository.save(testUser);

        // 사용자 토큰 생성
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        userToken = jwtTokenProvider.createToken(auth);

        application = new Application();
        application.setId("1");
        application.setName("Test User");
        application.setAddress("Test Address");
        application.setPhoneNum("01012345678");
        application.setServiceType(Application.ServiceType.AC_CLEANING);

        applicationResponse = ApplicationResponse.builder()
            .id("1")
            .name("Test User")
            .address("Test Address")
            .phoneNum("01012345678")
            .serviceType(Application.ServiceType.AC_CLEANING)
            .build();

        applicationListResponse = ApplicationListResponse.from(application);

        createApplicationRequest = new CreateApplicationRequest();
        createApplicationRequest.setName("Test User");
        createApplicationRequest.setAddress("Test Address");
        createApplicationRequest.setPhoneNum("010-1234-5678");
        createApplicationRequest.setServiceType(Application.ServiceType.AC_CLEANING);
        createApplicationRequest.setAcCondition("Test Condition");
        createApplicationRequest.setOutdoorUnitCleaning(true);

        applicationFileRequest = new ApplicationFileRequest();
        acConditionPhoto = new MockMultipartFile(
            "acConditionPhotos",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
        acStickerPhoto = new MockMultipartFile(
            "acStickerPhoto",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
        applicationFileRequest.setAcConditionPhotos(List.of(acConditionPhoto));
        applicationFileRequest.setAcStickerPhoto(acStickerPhoto);

        requestFile = new MockMultipartFile(
            "data",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(createApplicationRequest).getBytes()
        );
    }

    @Test
    @DisplayName("상담 신청 생성 성공")
    void createApplicationSuccess() throws Exception {
        mockMvc.perform(multipart("/api/application")
                .file(requestFile)
                .file(acConditionPhoto)
                .file(acStickerPhoto)
                .characterEncoding("UTF-8"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("상담 신청 목록 조회 성공 (관리자)")
    void getApplicationsSuccess() throws Exception {
        // 먼저 데이터를 하나 생성
        createApplicationSuccess();
        
        mockMvc.perform(get("/api/application")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("상담 신청 상세 조회 성공 (관리자)")
    void getApplicationSuccess() throws Exception {
        // 먼저 데이터를 하나 생성하고 ID를 얻음
        createApplicationSuccess();
        
        // 생성된 첫 번째 신청서를 조회
        mockMvc.perform(get("/api/application/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("상담 신청 목록 조회 실패 (일반 사용자)")
    void getApplicationsFailureForUser() throws Exception {
        // when & then
        mockMvc.perform(get("/api/application")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("상담 신청 상세 조회 실패 (일반 사용자)")
    void getApplicationFailureForUser() throws Exception {
        // when & then
        mockMvc.perform(get("/api/application/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 본인 신청 목록 조회 성공")
    void getMyApplicationsSuccess() throws Exception {
        // given
        setUpMockAuthentication();
        createApplicationSuccess(); // 먼저 신청서 생성

        // when & then
        mockMvc.perform(get("/api/application/my")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("사용자 본인 신청서 상세 조회 성공")
    void getMyApplicationSuccess() throws Exception {
        // given
        setUpMockAuthentication();
        createApplicationSuccess(); // 먼저 신청서 생성

        // when & then
        mockMvc.perform(get("/api/application/my/1")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 신청 목록 조회 실패")
    void getMyApplicationsFailureUnauthorized() throws Exception {
        mockMvc.perform(get("/api/application/my"))
            .andExpect(status().isUnauthorized());
    }
} 