package com.example.messageRouting.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "scenarioCrossRef")
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioCrossRef {
	@Field("_id")
    private String id;

    @Field("name")
    private String scenarioName;
}
