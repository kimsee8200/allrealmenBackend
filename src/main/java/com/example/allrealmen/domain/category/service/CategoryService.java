package com.example.allrealmen.domain.category.service;

import com.example.allrealmen.common.service.FileService;
import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.category.dto.CategoryResponse;
import com.example.allrealmen.domain.category.dto.UpdateCategoryRequest;
import com.example.allrealmen.domain.category.entity.Category;
import com.example.allrealmen.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private static final String CATEGORY_FILE_DIR = "categories";
    
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(CategoryResponse::from)
            .toList();
    }
    
    public CategoryResponse getCategory(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        return CategoryResponse.from(category);
    }
    
    @Transactional
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request,
                                        List<MultipartFile> images,
                                        List<MultipartFile> videos) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
            
        category.setDescription(request.getDescription());
        
        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<Category.FileInfo> imageInfos = new ArrayList<>();
            for (MultipartFile image : images) {
                String fileUrl = fileService.saveFile(image, CATEGORY_FILE_DIR);
                Category.FileInfo fileInfo = new Category.FileInfo();
                fileInfo.setUrl(fileUrl);
                imageInfos.add(fileInfo);
            }
            category.setImages(imageInfos);
        }
        
        // 비디오 처리
        if (videos != null && !videos.isEmpty()) {
            List<Category.FileInfo> videoInfos = new ArrayList<>();
            for (MultipartFile video : videos) {
                String fileUrl = fileService.saveFile(video, CATEGORY_FILE_DIR);
                Category.FileInfo fileInfo = new Category.FileInfo();
                fileInfo.setUrl(fileUrl);
                videoInfos.add(fileInfo);
            }
            category.setVideos(videoInfos);
        }
        
        return CategoryResponse.from(categoryRepository.save(category));
    }
    
    @Transactional
    public void initializeCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        
        List<Category> categories = new ArrayList<>();
        
        // 입주/이사 청소
        Category movingCategory = new Category();
        movingCategory.setCategory("MOVING_IN_CLEANING");
        movingCategory.setDescription("믿고 맡기는 친환경 입주이사청소, 이사입주 청소전문, 꼼꼼한 청소업체, A/S완벽보장, 믿고 맡기는 청소업체");
        categories.add(movingCategory);
        
        // 원투룸 청소
        Category oneroomCategory = new Category();
        oneroomCategory.setCategory("ONEROOM_CLEANING");
        oneroomCategory.setDescription("다중 및 다가구 원투룸 입주청소 전문업체");
        categories.add(oneroomCategory);
        
        // 에어컨 청소
        Category acCategory = new Category();
        acCategory.setCategory("AC_CLEANING");
        acCategory.setDescription("양심가격, 안심케어 쓱싹평균 1주 내로 청소완료\n에어컨청소업체추천 / 살균세척, 곰팡이제거 / 검증된 전문가에게 맡기세요");
        categories.add(acCategory);
        
        // 상가건물 정기청소
        Category buildingCategory = new Category();
        buildingCategory.setCategory("REGULAR_BUILDING_CLEANING");
        buildingCategory.setDescription("원투룸건물,건물,빌딩,사무실,학원,매장 등 정기적으로 관리가 필요한 곳에 주1~5회 숙련된 전문가가 파견되어 현장에 맞는 청소를 정기적으로 진행해 드리는 서비스입니다.");
        categories.add(buildingCategory);
        
        // 소파/매트리스 청소
        Category sofaCategory = new Category();
        sofaCategory.setCategory("SOFA_MATTRESS_CLEANING");
        sofaCategory.setDescription("더러워진 매트리스, 소파, 카페트 교체하지말고 청소하여 사용하세요 고객 만족도 최상, 믿고 맡기세요");
        categories.add(sofaCategory);
        
        // 정리정돈 서비스
        Category organizingCategory = new Category();
        organizingCategory.setCategory("ORGANIZING_SERVICE");
        organizingCategory.setDescription("정리정돈 서비스");
        categories.add(organizingCategory);
        
        categoryRepository.saveAll(categories);
    }
} 