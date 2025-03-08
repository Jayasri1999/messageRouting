package com.example.messageRouting.adapter;

import java.lang.reflect.Method;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;

@Component
public class Transform1Adapter extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	@Override
    public void configure() throws Exception {
		onException(Exception.class)
        .handled(true)
        .log("Exception occured in route: ${exception.message}");
        from("activemq:transform1.in")
            .process(exchange -> {
                // Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

                if (processFlow != null) {
                	String currentHopString = exchange.getIn().getHeader("nextHop", String.class);
        			ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopString);
                    invokeMethod(currentHop.getProcess(), exchange);

                 // Set the next hop
                    String nextHop = currentHop.getNextHop();
                    exchange.getIn().setHeader("nextHop", nextHop);
                    exchange.getIn().setHeader("nextQueue", processFlow.getHops().get(nextHop).getInputQueue());
                }
            })
            .toD("activemq:${header.nextQueue}");
    }
	
	public void transformProcess1(Exchange exchange) {
		try {
			log.info("++Inside transformProcess1++");
		} catch (Exception e) {
			log.error("Error processing transform Process1 in Transform1 Adapter", e);
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
