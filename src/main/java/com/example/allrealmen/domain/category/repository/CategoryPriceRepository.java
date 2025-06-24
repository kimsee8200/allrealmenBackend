package com.example.allrealmen.domain.category.repository;

import com.example.allrealmen.domain.category.entity.Category;
import com.example.allrealmen.domain.category.entity.CategoryPrice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryPriceRepository extends MongoRepository<CategoryPrice, String> {
    List<CategoryPrice> findByCategoryOrderByMinAreaAsc(Category category);
    void deleteByCategoryId(String categoryId);
} 