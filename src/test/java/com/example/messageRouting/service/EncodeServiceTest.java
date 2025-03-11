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
public class EncodeServiceTest {
	@InjectMocks
	EncodeService encodeService;
	@Mock
	Exchange exchange;
	@Mock
	Message message;
	@BeforeEach
    public void setup() {
        when(exchange.getIn()).thenReturn(message);
    }
	
	@Test
	 public void testBase64Encoder_Success() {
        // Given
        String originalString = "HelloWorld";
        when(message.getBody(String.class)).thenReturn(originalString);

        // When
        encodeService.base64Encoder(exchange);

        // Then: capture and assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(message).setBody(captor.capture());

        String encodedResult = captor.getValue();
        String expectedEncoded = Base64.getEncoder().encodeToString(originalString.getBytes(StandardCharsets.UTF_8));

        assertEquals(expectedEncoded, encodedResult, "Base64 encoded string should match");
    }
	
	@Test
	 public void testProcess() {
	        // Given
	        when(message.getHeader("encodeType", String.class)).thenReturn("base64Encoder");

	        String originalString = "TestMessage";
	        when(message.getBody(String.class)).thenReturn(originalString);

	        // When
	        encodeService.process(exchange);

	        // Then
	        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	        verify(message).setBody(captor.capture());

	        String encodedResult = captor.getValue();
	        String expectedEncoded = Base64.getEncoder().encodeToString(originalString.getBytes(StandardCharsets.UTF_8));

	        assertEquals(expectedEncoded, encodedResult);
	    }
}
