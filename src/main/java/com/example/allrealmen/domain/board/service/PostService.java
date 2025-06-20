package com.example.allrealmen.domain.board.service;

import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.board.dto.CreatePostRequest;
import com.example.allrealmen.domain.board.dto.PostResponse;
import com.example.allrealmen.domain.board.entity.Post;
import com.example.allrealmen.domain.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    
    public Page<PostResponse> getPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreateAtDesc(pageable)
                .map(PostResponse::from);
    }
    
    public PostResponse getPost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.increaseWatch();
        return PostResponse.from(post);
    }
    
    @Transactional
    public PostResponse createPost(CreatePostRequest post) {
        Post postData = Post.form(post);
        postData.setWriterId(SecurityUtil.getCurrentUserId());
        return PostResponse.from(postRepository.save(postData));
    }
    
    @Transactional
    public PostResponse updatePost(String id, CreatePostRequest updatedPost) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        if (!post.getWriterId().equals(SecurityUtil.getCurrentUserId())) {
            throw new IllegalStateException("게시글을 수정할 권한이 없습니다.");
        }
        
        post.setTitle(updatedPost.getTitle());
        post.setContent(updatedPost.getContent());
        return PostResponse.from(postRepository.save(post));
    }
    
    @Transactional
    public void deletePost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        if (!post.getWriterId().equals(SecurityUtil.getCurrentUserId())) {
            throw new IllegalStateException("게시글을 삭제할 권한이 없습니다.");
        }
        
        postRepository.delete(post);
    }
} 