//package com.example.messageRouting.adapter;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.core.MongoTemplate;
//
//@Configuration
//public class MongoConfig {
//    private final MongoTemplate mongoTemplate;
//
//    public MongoConfig(MongoTemplate mongoTemplate) {
//        this.mongoTemplate = mongoTemplate;
//    }
//
//    @Bean
//    public void testMongoConnection() {
//        try {
//            System.out.println("++++ Checking MongoDB Connection ++++");
//            System.out.println("Collections: " + mongoTemplate.getCollectionNames());
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("++++ MongoDB Connection Failed ++++");
//        }
//    }
//}
//
