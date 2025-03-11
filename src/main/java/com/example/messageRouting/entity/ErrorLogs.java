package com.example.messageRouting.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "errorLogs")
public class ErrorLogs {

	@Id
	@Field("_id")
    private String id;
	@Field("routeId")
	private String routeId;
	@Field("SourceEndpoint")
	private String SourceEndpoint;
	@Field("errorMessage")
    private String errorMessage;
	@Field("errorDetails")
    private String errorDetails;
	@Field("payload")
    private String payload;
	@Field("timestamp")
    private Date timestamp;
    public ErrorLogs() {}
    public ErrorLogs(String routeId, String SourceEndpoint, String errorMessage, String errorDetails, String payload, Date timestamp) {
        this.routeId = routeId;
        this.SourceEndpoint = SourceEndpoint;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
        this.payload = payload;
        this.timestamp = new Date();
    }
}
