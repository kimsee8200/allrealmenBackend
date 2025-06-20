package com.example.allrealmen.domain.category.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.category.dto.CategoryResponse;
import com.example.allrealmen.domain.category.dto.UpdateCategoryRequest;
import com.example.allrealmen.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategory(id)));
    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @Valid @RequestPart("data") UpdateCategoryRequest request,
            @RequestPart(value = "image", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) List<MultipartFile> videos) {
        return ResponseEntity.ok(ApiResponse.success(
            categoryService.updateCategory(id, request, images, videos)));
    }
    
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> initializeCategories() {
        categoryService.initializeCategories();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 