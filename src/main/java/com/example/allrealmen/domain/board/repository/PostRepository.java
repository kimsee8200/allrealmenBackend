package com.example.allrealmen.domain.board.repository;

import com.example.allrealmen.domain.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PostRepository extends MongoRepository<Post, String> {
    @Query(value = "{}", sort = "{ 'createAt' : -1 }")
    Page<Post> findAllByOrderByCreateAtDesc(Pageable pageable);
} 