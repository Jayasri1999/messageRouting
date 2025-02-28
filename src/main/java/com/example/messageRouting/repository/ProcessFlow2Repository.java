package com.example.messageRouting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.messageRouting.entity.ProcessFlow2;

@Repository
public interface ProcessFlow2Repository extends MongoRepository<ProcessFlow2, String> {
	@Query("{ '_id': ?0 }")
	Optional<ProcessFlow2> findById(String id);
    @Query(value = "{}", fields = "{_id: 1}")
    List<String> findAllIds();
}
