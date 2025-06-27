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

        Application application = Application.from(request);

        application.setImages(fileService.saveFiles(fileRequest.getCleaningAreaPhoto(), APPLICATION_FILE_DIR));
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    

    

    public Page<ApplicationListResponse> getMyApplications(Pageable pageable) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        return applicationRepository.findAllByPhoneNumOrderByApplicationTimeDesc(
            SecurityUtil.getCurrentUserPhoneNumber(), 
            pageable
        ).map(ApplicationListResponse::from);
    }

    public ApplicationResponse getMyApplication(String id) {
        String currentUserPhoneNumber = SecurityUtil.getCurrentUserPhoneNumber();
        Application application = applicationRepository.findByIdAndPhoneNum(id, currentUserPhoneNumber)
            .orElseThrow(() -> new IllegalArgumentException("해당 신청서를 찾을 수 없습니다."));
        return ApplicationResponse.from(application);
    }
} 