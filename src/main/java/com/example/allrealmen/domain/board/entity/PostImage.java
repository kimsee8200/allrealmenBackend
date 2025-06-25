package com.example.allrealmen.domain.board.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "post_images")
@NoArgsConstructor
public class PostImage {
    @Id
    private String id;
    private String imageUrl;
    private Post post;

    @Builder
    public PostImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPost(Post post) {
        this.post = post;
    }
} 