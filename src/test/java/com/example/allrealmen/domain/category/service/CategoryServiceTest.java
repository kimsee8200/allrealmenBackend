package com.example.allrealmen.domain.category.service;

import com.example.allrealmen.common.service.FileService;
import com.example.allrealmen.domain.category.dto.CategoryResponse;
import com.example.allrealmen.domain.category.dto.UpdateCategoryRequest;
import com.example.allrealmen.domain.category.entity.Category;
import com.example.allrealmen.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileService fileService;

    private Category category;
    private UpdateCategoryRequest updateCategoryRequest;
    private List<MultipartFile> images;
    private List<MultipartFile> videos;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId("1");
        category.setCategory("TEST_CATEGORY");
        category.setDescription("Test Description");

        updateCategoryRequest = new UpdateCategoryRequest();
        updateCategoryRequest.setDescription("Updated Description");

        images = new ArrayList<>();
        images.add(new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        ));

        videos = new ArrayList<>();
        videos.add(new MockMultipartFile(
            "video",
            "test.mp4",
            "video/mp4",
            "test video content".getBytes()
        ));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getAllCategoriesSuccess() {
        // given
        List<Category> categories = List.of(category);
        when(categoryRepository.findAll()).thenReturn(categories);

        // when
        List<CategoryResponse> result = categoryService.getAllCategories();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(category.getCategory());
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getCategorySuccess() {
        // given
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        // when
        CategoryResponse result = categoryService.getCategory(category.getId());

        // then
        assertThat(result.getCategory()).isEqualTo(category.getCategory());
        assertThat(result.getDescription()).isEqualTo(category.getDescription());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회")
    void getCategoryNotFound() {
        // given
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory("nonExistingId"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카테고리를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategorySuccess() {
        // given
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));
        when(fileService.saveFile(any(), any())).thenReturn("test-url");
        when(categoryRepository.save(any())).thenReturn(category);

        // when
        CategoryResponse result = categoryService.updateCategory(
            category.getId(), updateCategoryRequest, images, videos);

        // then
        assertThat(result.getCategory()).isEqualTo(category.getCategory());
        assertThat(result.getDescription()).isEqualTo(category.getDescription());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시도")
    void updateCategoryNotFound() {
        // given
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(
            "nonExistingId", updateCategoryRequest, images, videos))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카테고리를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("카테고리 초기화 성공")
    void initializeCategoriesSuccess() {
        // given
        when(categoryRepository.count()).thenReturn(0L);
        when(categoryRepository.saveAll(any())).thenReturn(new ArrayList<>());

        // when
        categoryService.initializeCategories();

        // then
        verify(categoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("이미 존재하는 카테고리 초기화 시도")
    void initializeCategoriesAlreadyExists() {
        // given
        when(categoryRepository.count()).thenReturn(1L);

        // when
        categoryService.initializeCategories();

        // then
        verify(categoryRepository, never()).saveAll(any());
    }
} 