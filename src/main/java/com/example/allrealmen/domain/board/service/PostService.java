package com.example.allrealmen.domain.board.service;

import com.example.allrealmen.common.service.FileService;
import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.board.dto.CreatePostRequest;
import com.example.allrealmen.domain.board.dto.PostListResponse;
import com.example.allrealmen.domain.board.dto.PostResponse;
import com.example.allrealmen.domain.board.entity.Post;
import com.example.allrealmen.domain.board.entity.PostImage;
import com.example.allrealmen.domain.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    private final FileService fileService;

    public Page<PostListResponse> getPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreateAtDesc(pageable)
                .map(PostListResponse::from);
    }
    
    public PostResponse getPost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.increaseWatch();
        return PostResponse.from(post);
    }
    
    @Transactional
    public PostResponse createPost(CreatePostRequest request, List<MultipartFile> images) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writerId(SecurityUtil.getCurrentUserId())
                .createdAt(LocalDateTime.now())
                .build();
        
        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = uploadAndCreatePostImages(images);
            post.addImages(postImages);
        }
        
        return PostResponse.from(postRepository.save(post));
    }
    
    @Transactional
    public PostResponse updatePost(String id, CreatePostRequest request, List<MultipartFile> images) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        if (!post.getWriterId().equals(SecurityUtil.getCurrentUserId())) {
            throw new IllegalStateException("게시글을 수정할 권한이 없습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = uploadAndCreatePostImages(images);
            post.addImages(postImages);
        }
        
        return PostResponse.from(postRepository.save(post));
    }
    
    private List<PostImage> uploadAndCreatePostImages(List<MultipartFile> images) {
        return images.stream()
                .map(image -> {
                    String imageUrl = fileService.saveFile(image,"post");
                    return PostImage.builder()
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deletePost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        String currentUserId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = SecurityUtil.hasAuthority("ROLE_ADMIN");
        
        if (!post.getWriterId().equals(currentUserId) && !isAdmin) {
            throw new IllegalStateException("게시글을 삭제할 권한이 없습니다.");
        }
        
        postRepository.delete(post);
    }
} 