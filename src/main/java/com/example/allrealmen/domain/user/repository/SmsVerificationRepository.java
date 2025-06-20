package com.example.allrealmen.domain.user.repository;

import com.example.allrealmen.domain.user.entity.SmsVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsVerificationRepository extends MongoRepository<SmsVerification, String> {
    Optional<SmsVerification> findFirstByPhoneNumberOrderByExpirationTimeDesc(String phoneNumber);
    
    @Transactional
    @Query("{'expirationTime': {'$lt': ?0}}")
    void deleteExpiredCodes(LocalDateTime now);
} 