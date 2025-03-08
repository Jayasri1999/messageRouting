package com.example.messageRouting.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.messageRouting.entity.CountryCrossRef;

@Repository
public interface CountryCrossRefRepository extends MongoRepository<CountryCrossRef, String> {
    Optional<CountryCrossRef> findByKey(String key); 
}
