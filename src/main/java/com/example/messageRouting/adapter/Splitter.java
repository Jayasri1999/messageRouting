package com.example.messageRouting.adapter;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.CategoryRoutingCache;

@Component
public class Splitter extends RouteBuilder{
	@Autowired
	CategoryRoutingCache categoryRoutingCache;
	@Override
    public void configure() throws Exception {
        from("activemq:splitter.in")
            .split().jsonpath("$.order.category.subcategories[0].items[*]")
            .process(exchange->{
            	String category=exchange.getIn().getHeader("category",String.class);
                String subCategory=exchange.getIn().getHeader("subCategory",String.class);
                String nextQueue=categoryRoutingCache.getCategoryRouting(category, subCategory).getSplitter().getSplitterOutputQueue();
                exchange.getIn().setHeader("nextHop", nextQueue);               
            })
            .toD("activemq:${header.nextHop}");
    }
}
