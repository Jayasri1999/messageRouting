package com.example.messageRouting.processor;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
public class ActionFigureToysProcessFlow extends RouteBuilder{
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
        from("activemq:kidsToys.actionFigures.in")
            .log("Processing Action Figure Toys Order")
            .process(exchange -> {
            	// Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);
                String currentHopString = exchange.getIn().getHeader("externalHop", String.class);
                log.info("++++externalHop:"+currentHopString);
        		ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopString);
        		invokeMethod(currentHop.getProcess(), exchange);
        		String category=exchange.getIn().getHeader("category",String.class);
                String subCategory=exchange.getIn().getHeader("subCategory",String.class);
                ProcessFlow.SubCategoryHop subCategoryHop = currentHop.getCategories().get(category).get(subCategory);
                String nextHop = subCategoryHop.getNextHop();
                exchange.getIn().setHeader("xsltContent", subCategoryHop.getXsltContent());
                exchange.getIn().setHeader("nextHop", nextHop);
                exchange.getIn().setHeader("nextQueue", subCategoryHop.getRoute().get(nextHop).getInputQueue());
            	})
            .toD("activemq:${header.nextQueue}");
    }
	
	public void actionFiguresProcessFlow(Exchange exchange) {
		try {
			log.info("+++++++In actionFiguresProcessFlow+++++++++++");
		} catch (Exception e) {
			log.error("Exception occured in actionFiguresProcessFlow: ",e);
		}
//		String jsonPayload = exchange.getIn().getBody(String.class);
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode order = mapper.readTree(jsonPayload);
//        ((ObjectNode) order.at("/order")).put("specialBadge", "Limited Edition");
//        log.info("+++++++Added limited edition badge for action figure order++++++++++");
//        exchange.getIn().setBody(mapper.writeValueAsString(order));
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
