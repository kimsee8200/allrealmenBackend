package com.example.allrealmen.domain.board.repository;

import com.example.allrealmen.domain.board.entity.Reply;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ReplyRepository extends MongoRepository<Reply, String> {
    @Query(value = "{ 'postId' : ?0 }", sort = "{ 'createAt' : 1 }")
    List<Reply> findAllByPostIdOrderByCreateAtAsc(String postId);
} 