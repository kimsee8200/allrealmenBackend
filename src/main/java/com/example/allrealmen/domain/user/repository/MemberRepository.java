package com.example.allrealmen.domain.user.repository;

import com.example.allrealmen.domain.user.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findById(String id);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsById(String id);
    Optional<Member> findByPhoneNumber(String phoneNumber);
} 