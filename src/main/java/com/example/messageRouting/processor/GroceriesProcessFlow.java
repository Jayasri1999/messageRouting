package com.example.messageRouting.processor;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.CategoryRoutingCache;
import com.example.messageRouting.service.ErrorLogsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class GroceriesProcessFlow extends RouteBuilder{
	@Autowired
	CategoryRoutingCache categoryRoutingCache;
	@Autowired
	ErrorLogsService errorLogsService;
	@Override
    public void configure() throws Exception{
		onException(Exception.class)
		.handled(true) // Mark the exception as handled
        .process(exchange -> {
        	String routeId=exchange.getFromRouteId().toString();
        	String sourceEndpoint=exchange.getFromEndpoint().toString();
            Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            String errorMessage = exception.getMessage();
            String errorDetails = exception.toString();
            String payload = exchange.getIn().getBody(String.class);
            Date date= new Date();
            Timestamp ts = new Timestamp(date.getTime());

            // Log the exception to MongoDB
            errorLogsService.logError(routeId, sourceEndpoint, errorMessage, errorDetails, payload, ts);

            // Log the exception to the console
            log.error("Error Code: {}, Error Message: {}, Error Details: {}", errorMessage, errorDetails);

            // Stop further processing
            exchange.setRouteStop(true);
        })
        .to("log:errorLog");
		from("activemq:groceries.freshProduce.in")
        .log("Processing FreshProduce Groceries Order")
        .process(exchange -> {
        	String methodName=exchange.getIn().getHeader("routingName",String.class);
        	invokeMethod(methodName, exchange);
        	exchange.getIn().setHeader("nextHop", categoryRoutingCache.getCategoryRouting("groceries", "freshProduce").getCatOutputQueue());     
        })
        .toD("activemq:${header.nextHop}");
	}
	
	public void groceriesProcessFlow(Exchange exchange) {
		try {
			String jsonPayload = exchange.getIn().getBody(String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode order = mapper.readTree(jsonPayload);
			double amount = order.at("/order/amount").asDouble();
			if (amount > 30) {
			    ((ObjectNode) order.at("/order")).put("loyaltyPoints", "50 Bonus Points");
			    System.out.println("++++++++++Added 50 loyalty points for groceries order++++++++++");
			}
			exchange.getIn().setBody(mapper.writeValueAsString(order));
		} catch (JsonMappingException e) {
			log.error("Error occured while mapping Json in groceriesProcessFlow: ", e);
		} catch (JsonProcessingException e) {
			log.error("Error occured while processing Json in groceriesProcessFlow: ",e);
		} catch (Exception e) {
            log.error("Unexpected error in groceriesProcessFlow: ", e);
        }
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
