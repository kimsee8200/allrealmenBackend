package com.example.allrealmen.domain.board.entity;

import com.example.allrealmen.domain.board.dto.CreatePostRequest;
import com.example.allrealmen.domain.user.entity.Member;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "posts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    
    @Id
    private String id;
    
    private String title;
    
    private String content;
    
    private String writerId;
    
    private List<PostImage> images = new ArrayList<>();
    
    private List<Reply> replies = new ArrayList<>();
    
    private int watch = 0;
    
    private int comment = 0;
    
    @Timestamp
    private LocalDateTime createAt;
    
    public void addImage(PostImage image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
    }

    public void addImages(List<PostImage> images) {
        this.images = new ArrayList<>();
        this.images.addAll(images);
    }
    
    public void addReply(Reply reply) {
        if (this.replies == null) {
            this.replies = new ArrayList<>();
        }
        this.replies.add(reply);
        this.comment++;
    }
    
    public void removeReply(Reply reply) {
        if (this.replies == null) {
            this.replies = new ArrayList<>();
        }
        this.replies.remove(reply);
        this.comment--;
    }
    
    public void increaseWatch() {
        this.watch++;
    }

    public static Post form(CreatePostRequest post) {
        Post postEntity = new Post();
        postEntity.setTitle(post.getTitle());
        postEntity.setContent(post.getContent());
        return postEntity;
    }
} 