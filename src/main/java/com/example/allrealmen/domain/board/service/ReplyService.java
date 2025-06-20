package com.example.allrealmen.domain.board.service;

import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.board.dto.CreateReplyRequest;
import com.example.allrealmen.domain.board.dto.ReplyResponse;
import com.example.allrealmen.domain.board.entity.Post;
import com.example.allrealmen.domain.board.entity.Reply;
import com.example.allrealmen.domain.board.repository.PostRepository;
import com.example.allrealmen.domain.board.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {
    
    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    
    public List<ReplyResponse> getReplies(String postId) {
        return replyRepository.findAllByPostIdOrderByCreateAtAsc(postId)
                .stream()
                .map(ReplyResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReplyResponse createReply(String postId, CreateReplyRequest comment) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        Reply reply = Reply.builder()
                .comment(comment.getComment())
                .writerId(SecurityUtil.getCurrentUserId())
                .postId(postId)
                .build();
        
        post.addReply(reply);
        return ReplyResponse.from(replyRepository.save(reply));
    }
    
    @Transactional
    public ReplyResponse updateReply(String id, String comment) {
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        if (!reply.getWriterId().equals(SecurityUtil.getCurrentUserId())) {
            throw new IllegalStateException("댓글을 수정할 권한이 없습니다.");
        }
        
        reply.setComment(comment);
        return ReplyResponse.from(replyRepository.save(reply));
    }
    
    @Transactional
    public void deleteReply(String id) {
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        if (!reply.getWriterId().equals(SecurityUtil.getCurrentUserId())) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }
        
        Post post = postRepository.findById(reply.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        post.removeReply(reply);
        replyRepository.delete(reply);
    }
} 