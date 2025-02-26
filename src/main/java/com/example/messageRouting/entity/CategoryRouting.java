package com.example.messageRouting.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import lombok.*;

@Data
@Document(collection = "categoryRouting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRouting {

	    @Field("category") 
	    private String categoryName;

	    @Field("subCategory")
	    private String subcategoryName;

	    @Field("routingName")
	    private String routingName;
	    
	    @Field("inputQueue")
	    private String catInputQueue;
	    
	    @Field("outputQueue")
	    private String catOutputQueue;
	    
	    @Field("xsltContent")
	    private String xslt_content;
	    
	    @Field("splitter")
	    private Splitter splitter;
	    
	    @Data
	    public static class Splitter {

	        @Field("inputQueue")
	        private String splitterInputQueue;

	        @Field("outputQueue")
	        private String splitterOutputQueue;
	    }



}

