package com.example.allrealmen.domain.category.repository;

import com.example.allrealmen.domain.category.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
} 