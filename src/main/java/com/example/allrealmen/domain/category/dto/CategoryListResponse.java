package com.example.allrealmen.domain.category.dto;

import com.example.allrealmen.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListResponse {
    private String id;
    private String name;
    private String description;
    private String mainImage;

    public static CategoryListResponse from(Category category) {
        return CategoryListResponse.builder()
                .id(category.getId())
                .name(category.getCategory())  // category 필드를 name으로 매핑
                .description(category.getDescription())
                .mainImage(category.getImages() != null && !category.getImages().isEmpty() 
                    ? category.getImages().get(0).getUrl()  // 첫 번째 이미지 URL을 mainImage로 사용
                    : null)
                .build();
    }
} 