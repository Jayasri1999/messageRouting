package com.example.messageRouting.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.messageRouting.entity.CategoryRouting;
import java.util.Optional;

@Repository
public interface CategoryRoutingRepository extends MongoRepository<CategoryRouting, String> {
    
    Optional<CategoryRouting> findByCategoryNameAndSubcategoryName(String category_name, String subcategory_name);
}
