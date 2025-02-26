package com.example.messageRouting.adapter;


import java.lang.reflect.Method;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;

@Component
public class ExitAdapter extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	
	@Override
    public void configure() throws Exception {
        from("activemq:exit.in")
            .process(exchange -> {
                // Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

                if (processFlow != null) {
                    // Execute the exit process
                    int currentHopIndex = exchange.getIn().getHeader("currentHopIndex", Integer.class);
                    ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopIndex+1);
                    invokeMethod(currentHop.getProcess(), exchange);

                    // Set the next hop
                    if (currentHopIndex + 1 < processFlow.getHops().size()) {
                        exchange.getIn().setHeader("nextHop", processFlow.getHops().get(currentHopIndex + 1).getOutputQueue());
                        exchange.getIn().setHeader("currentHopIndex", currentHopIndex + 1);
                    }
                }
            })
            .toD("activemq:${header.nextHop}");
    }
	
	public void exitProcess1(Exchange exchange) {
		log.info("++Inside exitProcess1++");

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
