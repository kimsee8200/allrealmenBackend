package com.example.allrealmen.domain.application.repository;

import com.example.allrealmen.domain.application.entity.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationRepository extends MongoRepository<Application, String> {
    Page<Application> findAllByOrderByApplicationTimeDesc(Pageable pageable);
} 