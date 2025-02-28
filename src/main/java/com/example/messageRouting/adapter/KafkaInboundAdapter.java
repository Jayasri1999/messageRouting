package com.example.messageRouting.adapter;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;

@Component
public class KafkaInboundAdapter extends RouteBuilder{
	@Autowired
	ProcessFlowCache processFlowCache;
	@Value("${kafka.brokers}")
    private String kafkaBrokers;
	@Override
	public void configure()  throws Exception{
		List<String> idsList= processFlowCache.getProcessFlowIdsByHopKey("KafkaInboundAdapter");
        for (String id : idsList) {
        	String processFlowId=id.substring(9,17);
        	log.info("++++++++++inputQueue:"+processFlowId);
        	configureInboundRoute(processFlowId);
        }
	}
	public void configureInboundRoute(String processFlowId){
		from("kafka:" +processFlowId+".in?brokers=" + kafkaBrokers)
		.log("Received message from input Queue: " + processFlowId)
		.process(exchange->{
			log.info("+++Before Inbound Process+++");
			log.info("Received message: {}", exchange.getIn().getBody(String.class));
			exchange.getIn().setHeader("processFlowId", processFlowId);
			ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);
			ProcessFlow.Hop currentHop = processFlow.getHops().get("KafkaInboundAdapter");
			invokeMethod(currentHop.getProcess(), exchange);
			log.info("+++After Inbound Process+++");
		})
		.toD("activemq:${header.nextHop}");
		
	}
	
	public void inboundProcess1(Exchange exchange) {
        // Fetch process flow details from cache or DB
		String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
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
            exchange.getIn().setHeader("scenario", processFlow.getScenario());
            exchange.getIn().setHeader("country", processFlow.getCountry());
            exchange.getIn().setHeader("instance", processFlow.getInstance());
            exchange.getIn().setHeader("category", categoryName);
            exchange.getIn().setHeader("subCategory", subCategoryName);

            // Set the first hop as the next hop
            if (processFlow.getHops() != null && !processFlow.getHops().isEmpty()) {
            	String nextHop = processFlow.getHops().get("ActiveMQInboundAdapter").getNextHop();
            	exchange.getIn().setHeader("nextHop", nextHop);
                exchange.getIn().setHeader("nextQueue", processFlow.getHops().get(nextHop).getInputQueue());
            }
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
