package com.example.allrealmen.domain.board.controller;

import com.example.allrealmen.domain.board.dto.CreatePostRequest;
import com.example.allrealmen.domain.board.dto.CreateReplyRequest;
import com.example.allrealmen.domain.board.dto.PostResponse;
import com.example.allrealmen.domain.board.dto.ReplyResponse;
import com.example.allrealmen.domain.board.service.PostService;
import com.example.allrealmen.domain.board.service.ReplyService;
import com.example.allrealmen.domain.user.security.CustomUserDetails;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private ReplyService replyService;

    private CreatePostRequest createPostRequest;
    private CreateReplyRequest createReplyRequest;

    @BeforeEach
    void setUp() {
        createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("테스트 제목");
        createPostRequest.setContent("테스트 내용");

        createReplyRequest = new CreateReplyRequest();
        createReplyRequest.setComment("New Comment");
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getPostsSuccess() throws Exception {
        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "10"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostSuccess() throws Exception {
        // 먼저 게시글을 하나 생성
        createPostSuccess();
        
        mockMvc.perform(get("/api/posts/1"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("게시글 작성 성공")
    void createPostSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        MockMultipartFile request = new MockMultipartFile(
            "createPostRequest",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(createPostRequest).getBytes()
        );

        mockMvc.perform(multipart("/api/posts")
                .file(file)
                .file(request))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("게시글 수정 성공")
    void updatePostSuccess() throws Exception {
        // 먼저 게시글을 하나 생성
        createPostSuccess();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        MockMultipartFile request = new MockMultipartFile(
            "createPostRequest",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(createPostRequest).getBytes()
        );

        mockMvc.perform(multipart("/api/posts/1")
                .file(file)
                .file(request)
                .with(req -> {
                    req.setMethod("PUT");
                    return req;
                }))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("게시글 삭제 성공")
    void deletePostSuccess() throws Exception {
        // 먼저 게시글을 하나 생성
        createPostSuccess();

        mockMvc.perform(delete("/api/posts/1"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getRepliesSuccess() throws Exception {
        // 먼저 게시글과 댓글을 하나 생성
        createPostSuccess();
        createReplySuccess();

        mockMvc.perform(get("/api/posts/1/replies"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 작성 성공")
    void createReplySuccess() throws Exception {
        // 먼저 게시글을 하나 생성
        createPostSuccess();

        mockMvc.perform(post("/api/posts/1/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReplyRequest)))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 삭제 성공")
    void deleteReplySuccess() throws Exception {
        // 먼저 게시글과 댓글을 하나 생성
        createPostSuccess();
        createReplySuccess();

        mockMvc.perform(delete("/api/posts/1/replies/1"))
            .andDo(print())
            .andDo(result -> {
                System.out.println("Response Body: " + result.getResponse().getContentAsString());
            })
            .andExpect(status().isOk());
    }
} 