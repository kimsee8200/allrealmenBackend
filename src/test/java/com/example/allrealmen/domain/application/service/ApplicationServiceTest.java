package com.example.allrealmen.domain.application.service;

import com.example.allrealmen.common.service.FileService;
import com.example.allrealmen.domain.application.dto.ApplicationFileRequest;
import com.example.allrealmen.domain.application.dto.ApplicationListResponse;
import com.example.allrealmen.domain.application.dto.ApplicationResponse;
import com.example.allrealmen.domain.application.dto.CreateApplicationRequest;
import com.example.allrealmen.domain.application.entity.Application;
import com.example.allrealmen.domain.application.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @InjectMocks
    private ApplicationService applicationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private FileService fileService;

    private Application application;
    private CreateApplicationRequest createApplicationRequest;
    private ApplicationFileRequest applicationFileRequest;
    private MockMultipartFile acConditionPhoto;
    private MockMultipartFile acStickerPhoto;

    @BeforeEach
    void setUp() {
        application = new Application();
        application.setId("1");
        application.setName("Test User");
        application.setAddress("Test Address");
        application.setPhoneNum("01012345678");
        application.setServiceType(Application.ServiceType.AC_CLEANING);

        createApplicationRequest = new CreateApplicationRequest();
        createApplicationRequest.setName("Test User");
        createApplicationRequest.setAddress("Test Address");
        createApplicationRequest.setPhoneNum("01012345678");
        createApplicationRequest.setServiceType(Application.ServiceType.AC_CLEANING);
        createApplicationRequest.setAcCondition("Test Condition");
        createApplicationRequest.setOutdoorUnitCleaning(true);

        applicationFileRequest = new ApplicationFileRequest();
        acConditionPhoto = new MockMultipartFile(
            "acConditionPhoto",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        acStickerPhoto = new MockMultipartFile(
            "acStickerPhoto",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        applicationFileRequest.setAcConditionPhotos(List.of(acConditionPhoto));
        applicationFileRequest.setAcStickerPhoto(acStickerPhoto);
    }

    @Test
    @DisplayName("상담 신청 목록 조회 성공")
    void getApplicationsSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Application> applicationPage = new PageImpl<>(List.of(application));
        when(applicationRepository.findAllByOrderByApplicationTimeDesc(any())).thenReturn(applicationPage);

        // when
        Page<ApplicationListResponse> result = applicationService.getApplications(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(application.getName());
    }

    @Test
    @DisplayName("상담 신청 상세 조회 성공")
    void getApplicationSuccess() {
        // given
        when(applicationRepository.findById(any())).thenReturn(Optional.of(application));

        // when
        ApplicationResponse result = applicationService.getApplication(application.getId());

        // then
        assertThat(result.getName()).isEqualTo(application.getName());
        assertThat(result.getAddress()).isEqualTo(application.getAddress());
    }

    @Test
    @DisplayName("존재하지 않는 상담 신청 조회")
    void getApplicationNotFound() {
        // given
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicationService.getApplication("nonExistingId"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("상담 신청을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("에어컨 청소 상담 신청 성공")
    void createAcCleaningApplicationSuccess() {
        // given
        when(fileService.saveFiles(any(), any())).thenReturn(List.of("test-url"));
        when(fileService.saveFile(any(), any())).thenReturn("test-url");
        when(applicationRepository.save(any())).thenReturn(application);

        // when
        ApplicationResponse result = applicationService.createApplication(
            createApplicationRequest, applicationFileRequest);

        // then
        assertThat(result.getName()).isEqualTo(application.getName());
        assertThat(result.getAddress()).isEqualTo(application.getAddress());
    }

    @Test
    @DisplayName("입주/이사 청소 상담 신청 성공")
    void createMovingInCleaningApplicationSuccess() {
        // given
        createApplicationRequest.setServiceType(Application.ServiceType.MOVING_IN_CLEANING);
        createApplicationRequest.setSquareMeters(30);
        createApplicationRequest.setPremiumCleaning(true);
        when(applicationRepository.save(any())).thenReturn(application);

        // when
        ApplicationResponse result = applicationService.createApplication(
            createApplicationRequest, applicationFileRequest);

        // then
        assertThat(result.getName()).isEqualTo(application.getName());
        assertThat(result.getAddress()).isEqualTo(application.getAddress());
    }
} 