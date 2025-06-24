package com.example.allrealmen.domain.category.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.category.dto.CategoryPriceRequest;
import com.example.allrealmen.domain.category.entity.CategoryPrice;
import com.example.allrealmen.domain.category.service.CategoryPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}/prices")
@RequiredArgsConstructor
public class CategoryPriceController {
    
    private final CategoryPriceService categoryPriceService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryPrice>>> getCategoryPrices(
            @PathVariable String categoryId) {
        return ResponseEntity.ok(ApiResponse.success(
            categoryPriceService.getCategoryPrices(categoryId)));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryPrice>> createCategoryPrice(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryPriceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            categoryPriceService.createCategoryPrice(categoryId, request)));
    }
    
    @DeleteMapping("/{priceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryPrice(
            @PathVariable String categoryId,
            @PathVariable String priceId) {
        categoryPriceService.deleteCategoryPrice(categoryId, priceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAllCategoryPrices(
            @PathVariable String categoryId) {
        categoryPriceService.deleteAllCategoryPrices(categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 