package com.example.messageRouting.adapter;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;

@Component
public class ActiveMQOutboundAdapater extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	
	@Override
    public void configure() throws Exception {
        from("activemq:outbound.in")
            .process(exchange -> {
            	log.info("+++++++++inside outbound adapter+++++++++");
                // Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

                if (processFlow != null) {
                    // Execute the exit process
                    int currentHopIndex = exchange.getIn().getHeader("currentHopIndex", Integer.class);

                    // Set the next hop
                    if (currentHopIndex + 1 < processFlow.getHops().size()) {
                        exchange.getIn().setHeader("nextHop", processFlow.getHops().get(currentHopIndex + 1).getOutputQueue());
                    }
                }
            })
            .log("Moving to output queue: ${header.nextHop}")
            .toD("activemq:${header.nextHop}");
    }
}
