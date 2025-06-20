package com.example.allrealmen.domain.category.controller;

import com.example.allrealmen.domain.category.dto.UpdateCategoryRequest;
import com.example.allrealmen.domain.category.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    private UpdateCategoryRequest updateCategoryRequest;
    private MockMultipartFile imageFile;
    private MockMultipartFile videoFile;
    private MockMultipartFile requestFile;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        updateCategoryRequest = new UpdateCategoryRequest();
        updateCategoryRequest.setDescription("Updated Description");

        imageFile = new MockMultipartFile(
            "image",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        videoFile = new MockMultipartFile(
            "video",
            "test.mp4",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "test video content".getBytes()
        );

        requestFile = new MockMultipartFile(
            "data",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(updateCategoryRequest).getBytes()
        );
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategoriesSuccess() throws Exception {
        mockMvc.perform(get("/api/categories"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getCategorySuccess() throws Exception {
        // 먼저 카테고리 초기화를 통해 데이터 생성
        initializeCategoriesSuccess();

        mockMvc.perform(get("/api/categories/1"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("카테고리 수정 성공")
    void updateCategorySuccess() throws Exception {
        // 먼저 카테고리 초기화를 통해 데이터 생성
        initializeCategoriesSuccess();

        mockMvc.perform(multipart("/api/categories/1")
                .file(imageFile)
                .file(videoFile)
                .file(requestFile)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("카테고리 초기화 성공")
    void initializeCategoriesSuccess() throws Exception {
        mockMvc.perform(post("/api/categories/initialize"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }
} 