package com.example.allrealmen.domain.application.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "applications")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    
    @Id
    private String id;
    
    private String name;
    private String address;
    private String phoneNum;
    private ServiceType serviceType;
    
    // 에어컨 청소 관련 필드
    private AcCleaningDetails acCleaningDetails;
    
    // 입주청소 관련 필드
    private MovingCleaningDetails movingCleaningDetails;
    
    // 공통 필드
    private Integer deposit = 100000;   // 예약금 (기본 10만원)
    private String depositAccount;      // 입금자명
    
    @CreatedDate
    private LocalDateTime applicationTime;
    
    public enum ServiceType {
        MOVING_IN_CLEANING("입주/이사청소"),
        ONEROOM_CLEANING("원투룸청소"),
        AC_CLEANING("에어컨/실외기청소"),
        REGULAR_BUILDING_CLEANING("상가건물정기청소"),
        SOFA_MATTRESS_CLEANING("소파/매트리스청소"),
        ORGANIZING_SERVICE("정리정돈서비스");
        
        private final String description;
        
        ServiceType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcCleaningDetails {
        private String[] acConditionPhotos;  // 에어컨 정면, 주변 사진 URL 배열
        private String acStickerPhoto;       // 제조 스티커 사진 URL
        private String acCondition;          // 에어컨 상태 및 수량
        private Boolean outdoorUnitCleaning; // 실외기 청소 유무
    }
    
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovingCleaningDetails {
        private Integer squareMeters;      // 평수
        private Boolean premiumCleaning;   // 프리미엄 특수청소 여부
    }
} 