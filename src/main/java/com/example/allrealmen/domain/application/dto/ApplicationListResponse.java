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
    private LocalDateTime applicationTime;
    private boolean isProcessed;     // 처리 여부
    
    public static ApplicationListResponse from(Application application) {
        ApplicationListResponse response = new ApplicationListResponse();
        response.setId(application.getId());
        response.setName(application.getName());
        response.setPhoneNum(application.getPhoneNum());
        response.setServiceType(application.getServiceType());
        response.setApplicationTime(application.getApplicationTime());
        
        return response;
    }
} 