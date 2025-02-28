package com.example.messageRouting.entity;

import java.util.Map;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.annotation.Nullable;
import lombok.*;

@Data
@Document(collection = "processFlow")
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
    private Map<String, Hop> hops;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hop {

        @Field("process")
        private String process;

        @Field("inputQueue")
        private String inputQueue;

        @Field("nextHop")
        @Nullable
        private String nextHop;

//        @Field("route")
//        private Map<String, RouteHop> route;
     // To handle category-subcategory mappings
        @Field("categories")
        @Nullable
        private Map<String, Map<String, SubCategoryHop>> categories; 
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubCategoryHop {

        @Field("process")
        private String process;

        @Field("inputQueue")
        private String inputQueue;

        @Field("nextHop")
        private String nextHop;
        
        @Field("xsltContent")
        @Nullable
        private String xsltContent;
        
     // Each subcategory can have its own route
        @Field("route")
        private Map<String, RouteHop> route; 
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteHop {

        @Field("process")
        private String process;

        @Field("inputQueue")
        private String inputQueue;

        @Field("nextHop")
        @Nullable
        private String nextHop;
    }
}
