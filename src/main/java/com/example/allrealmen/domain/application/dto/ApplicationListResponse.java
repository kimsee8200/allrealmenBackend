package com.example.allrealmen.domain.application.dto;

import com.example.allrealmen.domain.application.entity.Application;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ApplicationListResponse {
    private String id;
    private String name;
    private String phoneNum;
    private Application.ServiceType serviceType;
    private String serviceDetail;    // 서비스 상세 정보 (에어컨 수량 또는 평수)
    private LocalDateTime applicationTime;
    private boolean isProcessed;     // 처리 여부
    
    public static ApplicationListResponse from(Application application) {
        ApplicationListResponse response = new ApplicationListResponse();
        response.setId(application.getId());
        response.setName(application.getName());
        response.setPhoneNum(application.getPhoneNum());
        response.setServiceType(application.getServiceType());
        response.setApplicationTime(application.getApplicationTime());
        
        // 서비스 유형별 상세 정보 설정
        switch (application.getServiceType()) {
            case AC_CLEANING:
                if (application.getAcCleaningDetails() != null) {
                    response.setServiceDetail(application.getAcCleaningDetails().getAcCondition());
                }
                break;
            case MOVING_IN_CLEANING:
                if (application.getMovingCleaningDetails() != null) {
                    response.setServiceDetail(
                        application.getMovingCleaningDetails().getSquareMeters() + "평"
                    );
                }
                break;
            default:
                response.setServiceDetail("-");
        }
        
        return response;
    }
} 