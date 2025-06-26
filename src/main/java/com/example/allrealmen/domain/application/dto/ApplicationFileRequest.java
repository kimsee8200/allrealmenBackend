package com.example.allrealmen.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationFileRequest {
    private List<MultipartFile> acConditionPhotos;  // 에어컨 정면, 주변 사진
    private MultipartFile acStickerPhoto;          // 제조 스티커 사진
} 