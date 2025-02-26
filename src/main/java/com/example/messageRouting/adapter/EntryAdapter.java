package com.example.messageRouting.adapter;

import java.lang.reflect.Method;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;

@Component
public class EntryAdapter extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	@Override
    public void configure() throws Exception {
        from("activemq:entry.in")
            .process(exchange -> {
                // Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

                if (processFlow != null) {
                    // Execute the entry process
                    int currentHopIndex = exchange.getIn().getHeader("currentHopIndex", Integer.class);
                    log.info("+++++++++++++++in entry.in processFlow.getHops()+++++"+processFlow.getHops().get(0));
                    ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopIndex+1);
                    log.info("+++++++++++++++in entry.in currentProcess+++++"+currentHop.getProcess());
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
	
	public void entryProcess1(Exchange exchange) {
		log.info("++Inside EntryProcess1++");
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
