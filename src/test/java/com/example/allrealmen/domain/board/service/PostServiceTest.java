package com.example.allrealmen.domain.board.service;

import com.example.allrealmen.common.service.FileService;
import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.board.dto.CreatePostRequest;
import com.example.allrealmen.domain.board.dto.PostListResponse;
import com.example.allrealmen.domain.board.dto.PostResponse;
import com.example.allrealmen.domain.board.entity.Post;
import com.example.allrealmen.domain.board.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileService fileService;

    private Post post;
    private CreatePostRequest createPostRequest;
    private String currentUserId;
    private List<MultipartFile> mockImages;

    @BeforeEach
    void setUp() {
        currentUserId = "testUser";
        post = Post.builder()
            .id("1")
            .title("Test Title")
            .content("Test Content")
            .writerId(currentUserId)
            .build();

        createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("New Title");
        createPostRequest.setContent("New Content");

        mockImages = Arrays.asList(
            new MockMultipartFile(
                "image1",
                "test1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
            ),
            new MockMultipartFile(
                "image2",
                "test2.jpg",
                "image/jpeg",
                "test image content 2".getBytes()
            )
        );
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getPostsSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post));
        when(postRepository.findAllByOrderByCreateAtDesc(any())).thenReturn(postPage);

        // when
        Page<PostListResponse> result = postService.getPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(post.getTitle());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostSuccess() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.of(post));

        // when
        PostResponse result = postService.getPost(post.getId());

        // then
        assertThat(result.getTitle()).isEqualTo(post.getTitle());
        assertThat(result.getContent()).isEqualTo(post.getContent());
        verify(postRepository).findById(post.getId());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회")
    void getPostNotFound() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost("nonExistingId"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("게시글 작성 성공 - 이미지 없음")
    void createPostSuccessWithoutImage() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(postRepository.save(any())).thenReturn(post);

            // when
            PostResponse result = postService.createPost(createPostRequest, null);

            // then
            assertThat(result.getTitle()).isEqualTo(post.getTitle());
            assertThat(result.getContent()).isEqualTo(post.getContent());
            verify(postRepository).save(any(Post.class));
            verify(fileService, never()).saveFile(any(), any());
        }
    }

    @Test
    @DisplayName("게시글 작성 성공 - 이미지 포함")
    void createPostSuccessWithImage() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(postRepository.save(any())).thenReturn(post);
            when(fileService.saveFile(any(MultipartFile.class), eq("post")))
                .thenReturn("test-image-url-1", "test-image-url-2");

            // when
            PostResponse result = postService.createPost(createPostRequest, mockImages);

            // then
            assertThat(result.getTitle()).isEqualTo(post.getTitle());
            assertThat(result.getContent()).isEqualTo(post.getContent());
            verify(postRepository).save(any(Post.class));
            verify(fileService, times(2)).saveFile(any(MultipartFile.class), eq("post"));
        }
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePostSuccess() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(postRepository.findById(any())).thenReturn(Optional.of(post));
            when(postRepository.save(any())).thenReturn(post);

            // when
            PostResponse result = postService.updatePost(post.getId(), createPostRequest, null);

            // then
            assertThat(result.getTitle()).isEqualTo(post.getTitle());
            assertThat(result.getContent()).isEqualTo(post.getContent());
            verify(postRepository).save(any(Post.class));
        }
    }

    @Test
    @DisplayName("권한이 없는 게시글 수정 시도")
    void updatePostWithoutPermission() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn("otherUser");
            when(postRepository.findById(any())).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(post.getId(), createPostRequest, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("게시글을 수정할 권한이 없습니다.");
        }
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePostSuccess() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(postRepository.findById(any())).thenReturn(Optional.of(post));
            doNothing().when(postRepository).delete(any());

            // when
            postService.deletePost(post.getId());

            // then
            verify(postRepository).delete(post);
        }
    }

    @Test
    @DisplayName("권한이 없는 게시글 삭제 시도")
    void deletePostWithoutPermission() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn("otherUser");
            when(postRepository.findById(any())).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.deletePost(post.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("게시글을 삭제할 권한이 없습니다.");
        }
    }
} 