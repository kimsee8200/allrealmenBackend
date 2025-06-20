package com.example.allrealmen.domain.board.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {
    
    @Id
    private String id;
    
    private String url;
} 