package com.example.allrealmen.domain.board.dto;

import com.example.allrealmen.domain.board.entity.Reply;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyResponse {
    private String id;
    private String comment;
    private String writerId;
    private String postId;
    private LocalDateTime createAt;
    
    public static ReplyResponse from(Reply reply) {
        ReplyResponse response = new ReplyResponse();
        response.setId(reply.getId().toString());
        response.setComment(reply.getComment());
        response.setWriterId(reply.getWriterId());
        response.setPostId(reply.getPostId());
        response.setCreateAt(reply.getCreateAt());
        return response;
    }
} 