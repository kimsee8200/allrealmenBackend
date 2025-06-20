package com.example.allrealmen.domain.board.entity;

import com.example.allrealmen.domain.user.entity.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "replies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {
    
    @Id
    private String id;
    
    private String comment;
    
    private String writerId;
    
    private String postId;
    
    @CreatedDate
    private LocalDateTime createAt;
} 