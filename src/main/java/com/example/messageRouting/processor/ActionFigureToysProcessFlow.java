package com.example.messageRouting.processor;

import java.lang.reflect.Method;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
public class ActionFigureToysProcessFlow extends RouteBuilder{
	@Autowired
	ProcessFlowCache processFlowCache;
	@Override
    public void configure() throws Exception {
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
	
	public void actionFiguresProcessFlow(Exchange exchange) throws JsonMappingException, JsonProcessingException {
		log.info("+++++++In actionFiguresProcessFlow+++++++++++");
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
