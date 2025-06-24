package com.example.allrealmen.domain.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPriceRequest {
    
    @NotNull(message = "최소 평수를 입력해주세요.")
    @Min(value = 1, message = "최소 평수는 1평 이상이어야 합니다.")
    private Integer minArea;
    
    @NotNull(message = "최대 평수를 입력해주세요.")
    @Min(value = 1, message = "최대 평수는 1평 이상이어야 합니다.")
    private Integer maxArea;
    
    @NotNull(message = "서비스 시간을 입력해주세요.")
    @Min(value = 30, message = "서비스 시간은 30분 이상이어야 합니다.")
    private Integer duration; // 분 단위
    
    @NotNull(message = "가격을 입력해주세요.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;
} 