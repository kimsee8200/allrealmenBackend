package com.example.allrealmen.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateCategoryRequest {
    @NotBlank(message = "설명은 필수 입력값입니다.")
    private String description;
} 