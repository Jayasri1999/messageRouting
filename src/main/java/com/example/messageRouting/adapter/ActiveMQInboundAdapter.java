package com.example.messageRouting.adapter;

import java.lang.reflect.Method;
import java.util.List;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.entity.ProcessFlow.Service;


@Component
public class ActiveMQInboundAdapter  extends RouteBuilder{
	@Autowired
	ProcessFlowCache processFlowCache;
	@Autowired
	ApplicationContext applicationContext;
	@Override
	public void configure()  throws Exception{
		onException(Exception.class)
        .handled(true) 
        .log("Exception occurred in route: ${exception.message}");
		List<String> idsList= processFlowCache.getProcessFlowIdsByHopKey("ActiveMQInboundAdapter");
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
			log.info("Received message: {}", exchange.getIn().getBody(String.class));
			exchange.getIn().setHeader("processFlowId", processFlowId);
			ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);
			ProcessFlow.Hop currentHop = processFlow.getHops().get("ActiveMQInboundAdapter");
			List<Service> services= currentHop.getServices();
			if(!services.isEmpty()) {
				for(Service service: services) {
					if("CrossRefService".equals(service.getService())) {
						exchange.getIn().setHeader("keyXPath", service.getKeyXPath());
						exchange.getIn().setHeader("replaceXPath", service.getReplaceXPath());
						exchange.getIn().setHeader("refCollection", service.getRefCollection());
						log.info("+++++++++Before invoking crossRefService class Method++++++++++++++++++");
						invokeClassMethod("com.example.messageRouting.service."+service.getService(), "process", exchange);
					}else if ("DecodeService".equals(service.getService())) {
						exchange.getIn().setHeader("decodeType", service.getDecodeType());
						log.info("+++++++++Before invoking decode class Method++++++++++++++++++");
						invokeClassMethod("com.example.messageRouting.service."+service.getService(), "process", exchange);
					}
				}
			}
			log.info("+++Before Inbound Process+++");
			invokeMethod(currentHop.getProcess(), exchange);
			log.info("+++After Inbound Process+++");
		})
		.toD("activemq:${header.nextQueue}");
		
	}
	
	public void inboundProcess1(Exchange exchange) {
		try {
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
	        String country = processFlow.getCountry();

	        if (processFlow != null) {
	            // Set process flow details in headers
	            exchange.getIn().setHeader("scenario", processFlow.getScenario());
	            exchange.getIn().setHeader("country", country);
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
			
		} catch (Exception e) {
			log.error("Error processing inbound Process1", e);
		}
        
    }
	
//	public void countryCrossRefService(Exchange exchange) throws Exception {
//		String xPath= exchange.getIn().getHeader("xPath",String.class);
//		String country = exchange.getContext().resolveLanguage("xpath")
//                .createExpression(xPath)
//                .evaluate(exchange, String.class); 
//        CountryCrossRef countryCrossRef = countryCrossRefCache.getCountryCrossRefById(country);
//		String countryName= countryCrossRef.getCountryName();
//		String xmlBody = exchange.getIn().getBody(String.class);
//		// Parse XML into DOM
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = factory.newDocumentBuilder();
//		Document document = builder.parse(new InputSource(new StringReader(xmlBody)));
//
//		// Locate <country> element and update its value
//		NodeList countryNodes = document.getElementsByTagName("country");
//		if (countryNodes.getLength() > 0) {
//		    countryNodes.item(0).setTextContent(countryName);
//		}
//		// Convert DOM back to XML String
//		TransformerFactory transformerFactory = TransformerFactory.newInstance();
//		Transformer transformer = transformerFactory.newTransformer();
//		StringWriter writer = new StringWriter();
//		transformer.transform(new DOMSource(document), new StreamResult(writer));
//		String updatedXmlBody = writer.toString();
//
//		// Set updated XML back to Exchange
//		exchange.getIn().setBody(updatedXmlBody);	
//		
//	}
	

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
