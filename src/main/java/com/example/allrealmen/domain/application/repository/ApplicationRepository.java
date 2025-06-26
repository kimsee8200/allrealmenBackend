package com.example.allrealmen.domain.application.repository;

import com.example.allrealmen.domain.application.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ApplicationRepository extends MongoRepository<Application, String> {
    Page<Application> findAllByOrderByApplicationTimeDesc(Pageable pageable);
    Page<Application> findAllByPhoneNumOrderByApplicationTimeDesc(String phoneNum, Pageable pageable);
    Optional<Application> findByIdAndPhoneNum(String id, String phoneNum);
} 