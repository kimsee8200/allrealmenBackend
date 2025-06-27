package com.example.allrealmen.domain.application.dto;

import com.example.allrealmen.domain.application.entity.Application;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    @DateTimeFormat(fallbackPatterns = "yyyy-MM-dd HH:mm")
    private LocalDateTime wantedCleaningTime;
    
    // 에어컨 청소 관련 필드
    private Integer squareMeters;      // 평수

    private Boolean premiumCleaning;   // 프리미엄 특수청소 여부
}