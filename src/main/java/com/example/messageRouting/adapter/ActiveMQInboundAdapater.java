package com.example.messageRouting.adapter;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;


@Component
public class ActiveMQInboundAdapater  extends RouteBuilder{
	@Autowired
	ProcessFlowCache processFlowCache;
	@Override
	public void configure()  throws Exception{
		List<String> idsList= processFlowCache.getProcessFlowIds();
        for (String id : idsList) {
        	String processFlowId=id.substring(9,17);
        	log.info("++++++++++inputQueue:"+processFlowId);
        	configureInboundRoute(processFlowId);
        }
	}
	public void configureInboundRoute(String processFlowId){
		from("activemq:"+processFlowId+".in")
		.log("Received message from input Queue: " + processFlowId)
		.process(exchange->{
			log.info("+++Before Inbound Process+++");
			log.info("Received message: {}", exchange.getIn().getBody(String.class));
			inboundProcess(exchange, processFlowId);
			log.info("+++After Inbound Process+++");
		})
		.toD("activemq:${header.nextHop}");
		
	}
	
	public void inboundProcess(Exchange exchange, String processFlowId) {
        // Fetch process flow details from cache or DB
        String categoryName = exchange.getContext().resolveLanguage("xpath")
                .createExpression("/order/category/name/text()")
                .evaluate(exchange, String.class); 
        String subCategoryName = exchange.getContext().resolveLanguage("xpath")
                .createExpression("/order/category/subcategories/subcategory/name/text()")
                .evaluate(exchange, String.class); 

        log.info("categoryName: "+ categoryName+" subCategoryName: "+subCategoryName);
        ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

        if (processFlow != null) {
            // Set process flow details in headers
            exchange.getIn().setHeader("processFlowId", processFlowId);
            exchange.getIn().setHeader("scenario", processFlow.getScenario());
            exchange.getIn().setHeader("country", processFlow.getCountry());
            exchange.getIn().setHeader("instance", processFlow.getInstance());
            exchange.getIn().setHeader("category", categoryName);
            exchange.getIn().setHeader("subCategory", subCategoryName);

            // Set the first hop as the next hop
            if (processFlow.getHops() != null && !processFlow.getHops().isEmpty()) {
                exchange.getIn().setHeader("nextHop", processFlow.getHops().get(0).getOutputQueue());
             // Track the current hop index
                exchange.getIn().setHeader("currentHopIndex", 0); 
            }
        }
    }
	


}
