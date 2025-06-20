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
        Application application = new Application();
        application.setName(request.getName());
        application.setAddress(request.getAddress());
        application.setPhoneNum(request.getPhoneNum());
        application.setServiceType(request.getServiceType());
        application.setDepositAccount(request.getDepositAccount());
        
        // 서비스 유형별 필드 설정
        switch (request.getServiceType()) {
            case AC_CLEANING:
                handleAcCleaningFields(application, request, fileRequest);
                break;
            case MOVING_IN_CLEANING:
                handleMovingInCleaningFields(application, request);
                break;
            default:
                break;
        }
        
        return ApplicationResponse.from(applicationRepository.save(application));
    }
    
    private void handleAcCleaningFields(Application application, CreateApplicationRequest request, ApplicationFileRequest fileRequest) {
        Application.AcCleaningDetails details = new Application.AcCleaningDetails();
        
        if (fileRequest.getAcConditionPhotos() != null && !fileRequest.getAcConditionPhotos().isEmpty()) {
            details.setAcConditionPhotos(
                fileService.saveFiles(fileRequest.getAcConditionPhotos(), APPLICATION_FILE_DIR)
                    .toArray(new String[0])
            );
        }
        
        if (fileRequest.getAcStickerPhoto() != null) {
            details.setAcStickerPhoto(
                fileService.saveFile(fileRequest.getAcStickerPhoto(), APPLICATION_FILE_DIR)
            );
        }
        
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