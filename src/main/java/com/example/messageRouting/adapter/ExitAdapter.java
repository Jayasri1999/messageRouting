package com.example.messageRouting.adapter;


import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.service.ErrorLogsService;

@Component
public class ExitAdapter extends RouteBuilder{
	@Autowired
    ProcessFlowCache processFlowCache;
	@Autowired
	ErrorLogsService errorLogsService;
	@Override
    public void configure() throws Exception {
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
        from("activemq:exit.in")
        	.process(exchange -> {
        		String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);
                String externalHopString = exchange.getIn().getHeader("externalHop", String.class);
                if(externalHopString==null) {
                	String currentHopString = exchange.getIn().getHeader("nextHop", String.class);
                	ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopString);
                    invokeMethod(currentHop.getProcess(), exchange);

                 // Set the next hop
                    String nextHop = currentHop.getNextHop();
                    exchange.getIn().setHeader("nextHop", nextHop);
                    exchange.getIn().setHeader("nextQueue", processFlow.getHops().get(nextHop).getInputQueue());
                }else {
        		ProcessFlow.Hop externalHop = processFlow.getHops().get(externalHopString);
        		String category=exchange.getIn().getHeader("category",String.class);
                String subCategory=exchange.getIn().getHeader("subCategory",String.class);
                String routeHopString = exchange.getIn().getHeader("nextHop", String.class);
                ProcessFlow.RouteHop routeHop = externalHop.getCategories().get(category).get(subCategory).getRoute().get(routeHopString);
                invokeMethod(routeHop.getProcess(), exchange);
                String nextHop = routeHop.getNextHop();
                exchange.getIn().setHeader("nextHop", nextHop);
                exchange.getIn().setHeader("nextQueue", externalHop.getCategories().get(category).get(subCategory).getRoute().get(nextHop).getInputQueue());
                }
        	
        	})
        	.toD("activemq:${header.nextQueue}");
	}
	
	public void exitProcess1(Exchange exchange){
		try {
			log.info("+++++++In exitProcess1+++++++++++");
		} catch (Exception e) {
			log.error("Error processing exit Process1", e);
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
