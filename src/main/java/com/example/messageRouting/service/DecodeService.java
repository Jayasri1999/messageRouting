package com.example.messageRouting.service;

import java.lang.reflect.Method;
import java.util.Base64;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DecodeService {
	public void process(Exchange exchange) {
		String decodeType = exchange.getIn().getHeader("decodeType",String.class);
		invokeMethod(decodeType, exchange);	

	}
	
	public void base64Decoder(Exchange exchange) {
		try {
			String encodedBody = exchange.getIn().getBody(String.class);
			String decodedBody = new String(Base64.getDecoder().decode(encodedBody.trim()));
			exchange.getIn().setBody(decodedBody);
		} catch (Exception e) {
			log.error("Exception occured during base64 Decoding: ",e);
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
