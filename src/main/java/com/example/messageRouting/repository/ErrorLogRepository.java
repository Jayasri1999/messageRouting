package com.example.messageRouting.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.messageRouting.entity.ErrorLogs;

@Repository
public interface ErrorLogRepository extends MongoRepository<ErrorLogs, String> {
}
