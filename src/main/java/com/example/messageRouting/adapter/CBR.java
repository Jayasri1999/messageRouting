package com.example.messageRouting.adapter;

import java.lang.reflect.Method;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;

@Component
public class CBR extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	@Override
    public void configure() throws Exception {
		onException(Exception.class)
        .handled(true)
        .log("Exception occured in route: ${exception.message}");
		from("activemq:cbr.in")
			.process(exchange -> {
                // Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

                if (processFlow != null) {
                	String currentHopString = exchange.getIn().getHeader("nextHop", String.class);
            		ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopString);
                    log.info("+++++++++++++++in cbr.in+++++"+currentHop);
                    log.info("+++++++++++++++in cbr.in currentProcess+++++"+currentHop.getProcess());
                    invokeMethod(currentHop.getProcess(), exchange);

                    String category=exchange.getIn().getHeader("category",String.class);
                    String subCategory=exchange.getIn().getHeader("subCategory",String.class);
                    log.info("+++category:"+category+"subcategory:"+subCategory+"+++++++");
                    //get input queue based on category, subcategory
                    String nextQueue = currentHop.getCategories().get(category).get(subCategory).getInputQueue();
                    exchange.getIn().setHeader("nextQueue", nextQueue);
                    exchange.getIn().setHeader("externalHop",currentHopString);
                    log.info("+++in cbr:"+currentHopString);
                }
            })
            .toD("activemq:${header.nextQueue}");
	}
	
	public void cbrProcess1(Exchange exchange) {
		try {
			log.info("++Inside cbrProcess1++");
		} catch (Exception e) {
			log.error("Error processing cbr Process1", e);
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
