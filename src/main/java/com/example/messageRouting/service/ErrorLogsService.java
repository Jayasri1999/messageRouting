package com.example.messageRouting.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.entity.ErrorLogs;
import com.example.messageRouting.repository.ErrorLogRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ErrorLogsService {
	@Autowired
    private ErrorLogRepository errorLogRepository;

    public void logError(String routeId, String SourceEndpoint, String errorMessage, String errorDetails, String payload, Date timestamp) {
        ErrorLogs errorLog = new ErrorLogs(routeId, SourceEndpoint, errorMessage, errorDetails, payload, timestamp);
        log.info("++++++++Inside Error Log Service++++++++++");
        errorLogRepository.save(errorLog);
    }
}
