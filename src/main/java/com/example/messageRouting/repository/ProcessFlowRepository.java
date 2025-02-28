package com.example.messageRouting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.messageRouting.entity.ProcessFlow;

@Repository
public interface ProcessFlowRepository extends MongoRepository<ProcessFlow, String> {

    @Query("{ '_id': ?0 }")
    Optional<ProcessFlow> findById(String id);

    // Fetch all IDs where hops contain a specific key
    @Query(value = "{ 'hops.?0': { $exists: true } }", fields = "{ _id: 1 }")
    List<String> findAllIdsByHopKey(String hopKey);
}
