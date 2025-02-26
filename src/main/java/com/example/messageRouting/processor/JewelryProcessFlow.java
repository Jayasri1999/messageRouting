package com.example.messageRouting.processor;

import java.lang.reflect.Method;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.CategoryRoutingCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class JewelryProcessFlow extends RouteBuilder{
	@Autowired
	CategoryRoutingCache categoryRoutingCache;
	@Override
    public void configure() throws Exception {
        from("activemq:jewelry.diamonds.in")
            .log("Processing Diamonds Jewelry Order")
            .process(exchange -> {
            	String methodName=exchange.getIn().getHeader("routingName",String.class);
            	invokeMethod(methodName, exchange);
            	exchange.getIn().setHeader("nextHop", categoryRoutingCache.getCategoryRouting("jewelry", "diamonds").getCatOutputQueue());     
            })
            .toD("activemq:${header.nextHop}");
    }
	
	public void jewelryProcessFlow(Exchange exchange) throws JsonMappingException, JsonProcessingException {
		String jsonPayload = exchange.getIn().getBody(String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode order = mapper.readTree(jsonPayload);
        double amount = order.at("/order/amount").asDouble();
        if (amount > 1000) {
            ((ObjectNode) order.at("/order")).put("insurance", "Included");
            System.out.println("++++++++Added insurance for diamond jewelry order++++++++++");
        }
        exchange.getIn().setBody(mapper.writeValueAsString(order));
	}
	
	public void invokeMethod(String methodName, Object... params) {
	    try {
	        // Get method with exact name and parameter types
	        Class<?>[] paramTypes = new Class<?>[]{
	            Exchange.class
	        };	        
	        Method method = this.getClass().getMethod(methodName, paramTypes);
	        log.info("++++++++++invoke method++++++++++++++"+method);
	        method.invoke(this, params);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
