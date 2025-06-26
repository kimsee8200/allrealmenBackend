package com.example.allrealmen.domain.board.dto;

import com.example.allrealmen.domain.board.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostListResponse {
    private String id;
    private String title;
    private String writer;
    private int watch;
    private int comment;
    
    @JsonProperty("create_at")
    private LocalDateTime createAt;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writer(post.getWriterId())
                .watch(post.getWatch())
                .comment(post.getComment())
                .createAt(post.getCreatedAt())
                .build();
    }
} 