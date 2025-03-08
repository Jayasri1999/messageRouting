package com.example.messageRouting.service;

import java.lang.reflect.Method;
import java.util.Base64;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class EncodeService {
	
	public void process(Exchange exchange) {
		String encodeType = exchange.getIn().getHeader("encodeType",String.class);
		invokeMethod(encodeType, exchange);	
	}
	
	public void base64Encoder(Exchange exchange) {
		try {
			String xmlBody = exchange.getIn().getBody(String.class);
			String encodedBody = Base64.getEncoder().encodeToString(xmlBody.getBytes());
			exchange.getIn().setBody(encodedBody);
		} catch (Exception e) {
			log.error("Exception occured during base64 Encoding: ",e);
		}
	    
	}
	
	public void invokeMethod(String methodName, Object... params) {
	    try {
	        // Get method with exact name and parameter types
	        Class<?>[] paramTypes = new Class<?>[]{
	            Exchange.class
	        };	        
	        Method method = this.getClass().getMethod(methodName, paramTypes);
	        System.out.println("++++++++++invoke method++++++++++++++"+method);
	        method.invoke(this, params);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
