package com.example.allrealmen.domain.application.dto;

import com.example.allrealmen.domain.application.entity.Application;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime wantedCleaningTime;
    private Integer squareMeters;
    private LocalDateTime applicationTime;
    private List<String> images;
    
    public static ApplicationResponse from(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setName(application.getName());
        response.setAddress(application.getAddress());
        response.setPhoneNum(application.getPhoneNum());
        response.setServiceType(application.getServiceType());
        response.setApplicationTime(application.getApplicationTime());
        response.setWantedCleaningTime(application.getWantedCleaningTime());
        response.setSquareMeters(application.getSquareMeters());
        response.setImages(application.getImages());
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