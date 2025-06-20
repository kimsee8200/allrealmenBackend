package com.example.allrealmen.domain.application.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.application.dto.ApplicationFileRequest;
import com.example.allrealmen.domain.application.dto.ApplicationListResponse;
import com.example.allrealmen.domain.application.dto.ApplicationResponse;
import com.example.allrealmen.domain.application.dto.CreateApplicationRequest;
import com.example.allrealmen.domain.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationListResponse>>> getApplications(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplications(pageable)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplication(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplication(id)));
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @Valid @RequestPart("data") CreateApplicationRequest request,
            @ModelAttribute ApplicationFileRequest fileRequest) {
        return ResponseEntity.ok(ApiResponse.success(
            applicationService.createApplication(request, fileRequest)));
    }
} 