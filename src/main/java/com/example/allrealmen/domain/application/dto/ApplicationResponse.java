package com.example.allrealmen.domain.application.dto;

import com.example.allrealmen.domain.application.entity.Application;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private String id;
    private String name;
    private String address;
    private String phoneNum;
    private Application.ServiceType serviceType;
    
    // 에어컨 청소 관련 필드
    private AcCleaningDetailsResponse acCleaningDetails;
    
    // 입주청소 관련 필드
    private MovingCleaningDetailsResponse movingCleaningDetails;
    
    // 공통 필드
    private Integer deposit;
    private String depositAccount;
    private LocalDateTime applicationTime;
    
    public static ApplicationResponse from(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setName(application.getName());
        response.setAddress(application.getAddress());
        response.setPhoneNum(application.getPhoneNum());
        response.setServiceType(application.getServiceType());
        
        if (application.getAcCleaningDetails() != null) {
            response.setAcCleaningDetails(AcCleaningDetailsResponse.from(application.getAcCleaningDetails()));
        }
        
        if (application.getMovingCleaningDetails() != null) {
            response.setMovingCleaningDetails(MovingCleaningDetailsResponse.from(application.getMovingCleaningDetails()));
        }
        
        response.setDeposit(application.getDeposit());
        response.setDepositAccount(application.getDepositAccount());
        response.setApplicationTime(application.getApplicationTime());
        return response;
    }
    
    @Getter @Setter
    public static class AcCleaningDetailsResponse {
        private String[] acConditionPhotos;
        private String acStickerPhoto;
        private String acCondition;
        private Boolean outdoorUnitCleaning;
        
        public static AcCleaningDetailsResponse from(Application.AcCleaningDetails details) {
            AcCleaningDetailsResponse response = new AcCleaningDetailsResponse();
            response.setAcConditionPhotos(details.getAcConditionPhotos());
            response.setAcStickerPhoto(details.getAcStickerPhoto());
            response.setAcCondition(details.getAcCondition());
            response.setOutdoorUnitCleaning(details.getOutdoorUnitCleaning());
            return response;
        }
    }
    
    @Getter @Setter
    public static class MovingCleaningDetailsResponse {
        private Integer squareMeters;
        private Boolean premiumCleaning;
        
        public static MovingCleaningDetailsResponse from(Application.MovingCleaningDetails details) {
            MovingCleaningDetailsResponse response = new MovingCleaningDetailsResponse();
            response.setSquareMeters(details.getSquareMeters());
            response.setPremiumCleaning(details.getPremiumCleaning());
            return response;
        }
    }
} 