package com.example.allrealmen.domain.application.dto;

import com.example.allrealmen.domain.application.entity.Application;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
public class CreateApplicationRequest {
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;
    
    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String address;
    
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNum;
    
    @NotNull(message = "서비스 유형은 필수 입력값입니다.")
    private Application.ServiceType serviceType;
    
    // 에어컨 청소 관련 필드
    private List<MultipartFile> acConditionPhotos;  // 에어컨 정면, 주변 사진
    private MultipartFile acStickerPhoto;     // 제조 스티커 사진
    private String acCondition;        // 에어컨 상태 및 수량
    private Boolean outdoorUnitCleaning; // 실외기 청소 유무
    
    // 입주청소 관련 필드
    private Integer squareMeters;      // 평수
    private Boolean premiumCleaning;   // 프리미엄 특수청소 여부
    
    // 공통 필드
    private String depositAccount;      // 입금자명
} 