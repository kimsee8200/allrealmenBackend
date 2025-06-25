package com.example.allrealmen.domain.board.dto;

import com.example.allrealmen.domain.board.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponse {
    private String id;
    private String title;
    private String content;
    private String writer;
    private List<ImageResponse> images;
    private int watch;
    private int comment;
    private LocalDateTime createAt;
    
    public static PostResponse from(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setWriter(post.getWriterId());
        response.setImages(post.getImages() != null ? post.getImages().stream()
                .map(ImageResponse::from)
                .collect(Collectors.toList()) : null);
        response.setWatch(post.getWatch());
        response.setComment(post.getComment());
        response.setCreateAt(post.getCreateAt());
        return response;
    }
    
    @Getter @Setter
    public static class ImageResponse {
        private String id;
        private String url;
        
        public static ImageResponse from(com.example.allrealmen.domain.board.entity.PostImage image) {
            ImageResponse response = new ImageResponse();
            response.setId(image.getId());
            response.setUrl(image.getImageUrl());
            return response;
        }
    }
} 