package com.example.messageRouting.service;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DecodeServiceTest {
	@InjectMocks
	DecodeService decodeService;
	@Mock
	Exchange exchange;
	@Mock
	Message message;
	@BeforeEach
    public void setup() {
        decodeService = new DecodeService();
        exchange = mock(Exchange.class);
        message = mock(Message.class);

        // When exchange.getIn() is called, return the mocked message
        when(exchange.getIn()).thenReturn(message);
    }

	
	 @Test
	    public void testBase64Decoder_Success() {
	        String originalString = "HelloWorld";
	        String encodedString = Base64.getEncoder().encodeToString(originalString.getBytes(StandardCharsets.UTF_8));

	        // Set up the mock to return the encoded body
	        when(exchange.getIn().getBody(String.class)).thenReturn(encodedString);

	        // Act
	        decodeService.base64Decoder(exchange);

	        // Capture the decoded body
	        verify(message).setBody(anyString());  // Interaction check (optional)

	        // Assert that the correct decoded string is being set
	        // You can do this by using an ArgumentCaptor (from Mockito)
	        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	        verify(message).setBody(captor.capture());

	        String decodedResult = captor.getValue();
	        assertEquals(originalString, decodedResult);
	    }
	 @Test
	    public void testProcess() {
	        when(message.getHeader("decodeType", String.class)).thenReturn("base64Decoder");

	        // Also need to mock the body for base64 decoding
	        String originalString = "HelloWorld";
	        String encodedString = Base64.getEncoder().encodeToString(originalString.getBytes(StandardCharsets.UTF_8));
	        when(message.getBody(String.class)).thenReturn(encodedString);

	        decodeService.process(exchange);

	        // Verify interaction and assert
	        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	        verify(message).setBody(captor.capture());

	        String decodedResult = captor.getValue();
	        assertEquals(originalString, decodedResult);
	    }

	

}
