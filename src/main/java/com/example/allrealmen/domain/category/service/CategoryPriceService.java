package com.example.allrealmen.domain.category.service;

import com.example.allrealmen.domain.category.dto.CategoryPriceRequest;
import com.example.allrealmen.domain.category.entity.Category;
import com.example.allrealmen.domain.category.entity.CategoryPrice;
import com.example.allrealmen.domain.category.repository.CategoryPriceRepository;
import com.example.allrealmen.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryPriceService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryPriceRepository categoryPriceRepository;
    
    public List<CategoryPrice> getCategoryPrices(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        return categoryPriceRepository.findByCategoryOrderByMinAreaAsc(category);
    }
    
    @Transactional
    public CategoryPrice createCategoryPrice(String categoryId, CategoryPriceRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        
        validatePriceRequest(request);
        
        CategoryPrice categoryPrice = CategoryPrice.builder()
                .category(category)
                .minArea(request.getMinArea())
                .maxArea(request.getMaxArea())
                .duration(request.getDuration())
                .price(request.getPrice())
                .build();
                
        return categoryPriceRepository.save(categoryPrice);
    }
    
    @Transactional
    public void deleteCategoryPrice(String categoryId, String priceId) {
        CategoryPrice categoryPrice = categoryPriceRepository.findById(priceId)
                .orElseThrow(() -> new IllegalArgumentException("가격 정보를 찾을 수 없습니다."));
                
        if (!categoryPrice.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("해당 카테고리의 가격 정보가 아닙니다.");
        }
        
        categoryPriceRepository.delete(categoryPrice);
    }
    
    @Transactional
    public void deleteAllCategoryPrices(String categoryId) {
        categoryPriceRepository.deleteByCategoryId(categoryId);
    }
    
    private void validatePriceRequest(CategoryPriceRequest request) {
        if (request.getMinArea() > request.getMaxArea()) {
            throw new IllegalArgumentException("최소 평수가 최대 평수보다 클 수 없습니다.");
        }
    }
} 