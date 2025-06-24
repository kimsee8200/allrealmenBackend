package com.example.allrealmen.domain.category.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "category_prices")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPrice {
    
    @Id
    private String id;
    
    @DBRef
    private Category category;
    
    @Field("min_area")
    private Integer minArea; // 최소 평수
    
    @Field("max_area")
    private Integer maxArea; // 최대 평수
    
    @Field("duration")
    private Integer duration; // 분 단위로 저장
    
    @Field("price")
    private Integer price;
    
    public void updatePrice(Integer price) {
        this.price = price;
    }
    
    public void updateDuration(Integer duration) {
        this.duration = duration;
    }
    
    public void updateAreaRange(Integer minArea, Integer maxArea) {
        this.minArea = minArea;
        this.maxArea = maxArea;
    }
} 