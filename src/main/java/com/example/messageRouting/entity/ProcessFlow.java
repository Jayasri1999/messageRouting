package com.example.messageRouting.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.annotation.Nullable;
import lombok.*;

@Data
@Document(collection = "processFlow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessFlow {
	@Field("_id")
	private String id;

    @Field("scenario")
    private String scenario;

    @Field("country")
    private String country;

    @Field("instance")
    private int instance;

    @Field("hops")
    private List<Hop> hops;
    
    @Data
    public static class Hop {

        @Field("hopName")
        private String hopName;

        @Field("inputQueue")
        private String inputQueue;

        @Field("outputQueue")
        @Nullable
        private String outputQueue;

        @Field("process")
        @Nullable
        private String process;
    }

}
