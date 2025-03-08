package com.example.messageRouting.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.messageRouting.entity.ScenarioCrossRef;

@Repository
public interface ScenarioCrossRefRepository extends MongoRepository<ScenarioCrossRef, String>{
	Optional<ScenarioCrossRef> findById(String id);
}
