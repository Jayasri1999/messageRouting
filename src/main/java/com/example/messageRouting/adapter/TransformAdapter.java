package com.example.messageRouting.adapter;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.adapter.cache.CategoryRoutingCache;
import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.CategoryRouting;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.service.ErrorLogsService;

@Component
public class TransformAdapter extends RouteBuilder{
	@Autowired
    private ProcessFlowCache processFlowCache;
	@Autowired
	private CategoryRoutingCache categoryRoutingCache;
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
        from("activemq:transform.in")
            .process(exchange -> {
                // Fetch process flow details from headers
                String processFlowId = exchange.getIn().getHeader("processFlowId", String.class);
                ProcessFlow processFlow = processFlowCache.getProcessFlowById(processFlowId);

                if (processFlow != null) {
                	//Set xslt content
                	String category=exchange.getIn().getHeader("category",String.class);
                    String subCategory=exchange.getIn().getHeader("subCategory",String.class);
                    CategoryRouting categoryRouting=categoryRoutingCache.getCategoryRouting(category, subCategory);
                	exchange.getIn().setHeader("xsltContent", categoryRouting.getXslt_content());
                	exchange.getIn().setHeader("routingName", categoryRouting.getRoutingName());
                    // Execute the transform process
                    int currentHopIndex = exchange.getIn().getHeader("currentHopIndex", Integer.class);
                    ProcessFlow.Hop currentHop = processFlow.getHops().get(currentHopIndex+1);
                    invokeMethod(currentHop.getProcess(), exchange);

                    // Set the next hop
                    if (currentHopIndex + 1 < processFlow.getHops().size()) {
                        exchange.getIn().setHeader("currentHopIndex", currentHopIndex + 1);
                    }
                    
                    String nextQueue=category+"."+subCategory+".in";
                    exchange.getIn().setHeader("nextHop", nextQueue);
                }
            })
            .toD("activemq:${header.nextHop}");
    }
	
	public void transformProcess1(Exchange exchange) {
		try {
			// transform xml to json using XSLT
			String xsltContent = exchange.getIn().getHeader("xsltContent", String.class);
			String body = exchange.getIn().getBody(String.class);
			try {
			String transformedBody = xmlToJsonTransformXSLT(body, xsltContent);
			log.info("+++++++++++++++++++++"+transformedBody);
			exchange.getIn().setBody(transformedBody);
			} catch (TransformerException e) {
			    log.error("Error during XSLT transformation", e);
			    throw new RuntimeException("Transformation failed", e);
			}
		} catch (Exception e) {
			log.error("Error processing transform Process1 in Transform Adapter", e);
		}
        
	}
	
	public String xmlToJsonTransformXSLT(String body, String xsltContent) throws TransformerException  {
		TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(
            new StreamSource(new StringReader(xsltContent))
        );
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(body)), new StreamResult(writer));
        return writer.toString();
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
