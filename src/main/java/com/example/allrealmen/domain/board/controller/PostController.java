package com.example.allrealmen.domain.board.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.board.dto.CreatePostRequest;
import com.example.allrealmen.domain.board.dto.CreateReplyRequest;
import com.example.allrealmen.domain.board.dto.PostResponse;
import com.example.allrealmen.domain.board.dto.ReplyResponse;
import com.example.allrealmen.domain.board.service.PostService;
import com.example.allrealmen.domain.board.service.ReplyService;
import com.example.allrealmen.domain.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    private final ReplyService replyService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize() == 20 ? 10 : pageable.getPageSize(),  // 기본값 20을 10으로 변경
            pageable.getSort()
        );
        return ResponseEntity.ok(ApiResponse.success(postService.getPosts(pageRequest)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPost(id)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestPart(value = "request") @Valid CreatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(ApiResponse.success(
            postService.createPost(request, images)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable String id,
            @RequestPart(value = "request") @Valid CreatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(ApiResponse.success(
            postService.updatePost(id, request, images)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<ReplyResponse>>> getReplies(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(replyService.getReplies(id)));
    }
    
    @PostMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            @PathVariable String id,
            @Valid @RequestBody CreateReplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            replyService.createReply(id, request)));
    }
    
    @DeleteMapping("/{postId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @PathVariable String postId,
            @PathVariable String replyId) {
        replyService.deleteReply(replyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 