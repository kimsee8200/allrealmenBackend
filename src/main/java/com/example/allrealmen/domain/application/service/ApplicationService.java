package com.example.allrealmen.domain.application.service;

import com.example.allrealmen.common.service.FileService;
import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.application.dto.ApplicationFileRequest;
import com.example.allrealmen.domain.application.dto.ApplicationListResponse;
import com.example.allrealmen.domain.application.dto.ApplicationResponse;
import com.example.allrealmen.domain.application.dto.CreateApplicationRequest;
import com.example.allrealmen.domain.application.entity.Application;
import com.example.allrealmen.domain.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final FileService fileService;
    private static final String APPLICATION_FILE_DIR = "applications";
    
    public Page<ApplicationListResponse> getApplications(Pageable pageable) {
        return applicationRepository.findAllByOrderByApplicationTimeDesc(pageable)
                .map(ApplicationListResponse::from);
    }
    
    public ApplicationResponse getApplication(String id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상담 신청을 찾을 수 없습니다."));
        return ApplicationResponse.from(application);
    }
    
    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request, ApplicationFileRequest fileRequest) {
        validateCommonFields(request);
        
        Application application = new Application();
        application.setName(request.getName());
        application.setAddress(request.getAddress());
        application.setPhoneNum(request.getPhoneNum());
        application.setServiceType(request.getServiceType());
        application.setDepositAccount(request.getDepositAccount());
        
        // 서비스 유형별 필드 설정
        switch (request.getServiceType()) {
            case AC_CLEANING:
                validateAcCleaningFields(request, fileRequest);
                handleAcCleaningFields(application, request, fileRequest);
                break;
            case MOVING_IN_CLEANING:
                validateMovingInCleaningFields(request);
                handleMovingInCleaningFields(application, request);
                break;
            default:
                break;
        }
        
        return ApplicationResponse.from(applicationRepository.save(application));
    }
    
    private void validateCommonFields(CreateApplicationRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("이름은 필수 입력값입니다.");
        }
        if (!StringUtils.hasText(request.getAddress())) {
            throw new IllegalArgumentException("주소는 필수 입력값입니다.");
        }
        if (!StringUtils.hasText(request.getPhoneNum())) {
            throw new IllegalArgumentException("전화번호는 필수 입력값입니다.");
        }
        if (!request.getPhoneNum().matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다.");
        }
        if (!StringUtils.hasText(request.getDepositAccount())) {
            throw new IllegalArgumentException("입금자명은 필수 입력값입니다.");
        }
    }
    
    private void validateAcCleaningFields(CreateApplicationRequest request, ApplicationFileRequest fileRequest) {
        if (fileRequest == null) {
            throw new IllegalArgumentException("에어컨 사진은 필수입니다.");
        }
        if (fileRequest.getAcConditionPhotos() == null || fileRequest.getAcConditionPhotos().isEmpty()) {
            throw new IllegalArgumentException("에어컨 사진은 필수입니다.");
        }
        if (fileRequest.getAcStickerPhoto() == null) {
            throw new IllegalArgumentException("에어컨 스티커 사진은 필수입니다.");
        }
        if (!StringUtils.hasText(request.getAcCondition())) {
            throw new IllegalArgumentException("에어컨 상태 및 수량 정보는 필수입니다.");
        }
        if (request.getOutdoorUnitCleaning() == null) {
            throw new IllegalArgumentException("실외기 청소 여부는 필수 선택사항입니다.");
        }
    }
    
    private void validateMovingInCleaningFields(CreateApplicationRequest request) {
        if (request.getSquareMeters() == null || request.getSquareMeters() <= 0) {
            throw new IllegalArgumentException("평수는 0보다 커야 합니다.");
        }
        if (request.getPremiumCleaning() == null) {
            throw new IllegalArgumentException("프리미엄 청소 여부는 필수 선택사항입니다.");
        }
    }
    
    private void handleAcCleaningFields(Application application, CreateApplicationRequest request, ApplicationFileRequest fileRequest) {
        Application.AcCleaningDetails details = new Application.AcCleaningDetails();
        
        details.setAcConditionPhotos(
            fileService.saveFiles(fileRequest.getAcConditionPhotos(), APPLICATION_FILE_DIR)
                .toArray(new String[0])
        );
        
        details.setAcStickerPhoto(
            fileService.saveFile(fileRequest.getAcStickerPhoto(), APPLICATION_FILE_DIR)
        );
        
        details.setAcCondition(request.getAcCondition());
        details.setOutdoorUnitCleaning(request.getOutdoorUnitCleaning());
        
        application.setAcCleaningDetails(details);
    }
    
    private void handleMovingInCleaningFields(Application application, CreateApplicationRequest request) {
        Application.MovingCleaningDetails details = new Application.MovingCleaningDetails();
        details.setSquareMeters(request.getSquareMeters());
        details.setPremiumCleaning(request.getPremiumCleaning());
        
        application.setMovingCleaningDetails(details);
    }
} 