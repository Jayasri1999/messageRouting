package com.example.messageRouting.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "countryCrossRef")
@NoArgsConstructor
@AllArgsConstructor
public class CountryCrossRef {
	@Field("_id")
    private String id;
	
    @Field("key")
    private String key;

    @Field("value")
    private String value;
}
