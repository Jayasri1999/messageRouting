package com.example.messageRouting.adapter;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.entity.ProcessFlow.Service;
import com.example.messageRouting.service.ErrorLogsService;

@Component
public class ActiveMQOutboundAdapter extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	@Autowired
	ApplicationContext applicationContext;
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
        from("activemq:outbound.in")
            .process(exchange -> {
            	String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);
                String externalHopString = exchange.getIn().getHeader("externalHop", String.class);
                if(externalHopString==null) {
                	String currentHopString = exchange.getIn().getHeader("nextHop", String.class);
                	ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopString);
                	List<Service> services= currentHop.getServices();
        			if(!services.isEmpty()) {
        				for(Service service: services) {
        					if ("EncodeService".equals(service.getService())) {
        						exchange.getIn().setHeader("encodeType", service.getEncodeType());
        						log.info("+++++++++Before invoking Ecode class Method++++++++++++++++++");
        						invokeClassMethod("com.example.messageRouting.service."+service.getService(), "process", exchange);
        					}
        				}
        			}
                    invokeMethod(currentHop.getProcess(), exchange);
                }else {
        		ProcessFlow.Hop externalHop = processFlow.getHops().get(externalHopString);
        		String category=exchange.getIn().getHeader("category",String.class);
                String subCategory=exchange.getIn().getHeader("subCategory",String.class);
                String routeHopString = exchange.getIn().getHeader("nextHop", String.class);
                ProcessFlow.RouteHop routeHop = externalHop.getCategories().get(category).get(subCategory).getRoute().get(routeHopString);
                invokeMethod(routeHop.getProcess(), exchange);
                }
            })
            .toD("activemq:${header.nextQueue}");
    }
	
	public void outboundProcess1(Exchange exchange) {
		try {
			log.info("++Inside outboundProcess1++");
			String scenario = exchange.getIn().getHeader("scenario", String.class);
	        String country = exchange.getIn().getHeader("country", String.class);
	        Integer instance = exchange.getIn().getHeader("instance", Integer.class);
	     // Store the message in the corresponding dynamic queue output based on headers
	        String nextQueue = scenario + "." + country + "." + instance+".out";
	        exchange.getIn().setHeader("nextQueue", nextQueue);
			
		} catch (Exception e) {
			log.error("Error processing outbound Process1", e);
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
	public void invokeClassMethod(String className, String methodName, Object... params) {
	    try {
	        // Load the class dynamically
	        Class<?> clazz = Class.forName(className);
	        
	     // Create a new instance
//	        Object instance = clazz.getDeclaredConstructor().newInstance();
	     // Retrieve the bean from the Spring application context
            Object instance = applicationContext.getBean(clazz);
	        Class<?>[] paramTypes = new Class<?>[]{
	            Exchange.class
	        };	
	        Method method = clazz.getMethod(methodName, paramTypes);
	        log.info("++++++++++invoke class method++++++++++++++"+method);
	        Object result = method.invoke(instance, params);
	        System.out.println("Method Result: " + result);
	    } catch (Exception e) {
	        log.error("Unexpected error during reflection call", e);
	        e.printStackTrace();
	    }
	}
}
