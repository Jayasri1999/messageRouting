package com.example.messageRouting.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
import static org.junit.jupiter.api.Assertions.*;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.service.ErrorLogsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.internal.filter.ValueNodes.JsonNode;

@ExtendWith(MockitoExtension.class)
public class ShoeProcessFlowTest extends CamelTestSupport{
	@InjectMocks
	ShoeProcessFlow shoeProcessFlow;
	
	@Mock
    private ProcessFlowCache processFlowCache;

    @Mock
    private ErrorLogsService errorLogsService;

    @Mock
    private CamelContext camelContext;
    @Mock
    private Exchange exchange;

    @Mock
    private Message message;
    @Captor
    private ArgumentCaptor<String> bodyCaptor;
    @BeforeEach
    void initMocks() {
        shoeProcessFlow.setCamelContext(new DefaultCamelContext());
    }
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> shoeProcessFlow.configure());
    }
    
    @Test
    void testShoeProcessFlow_DiscountApplied() throws Exception {
        // Input JSON where amount > 100
        String inputJson = """
            {
                "order": {
                    "productId": "S123",
                    "productName": "Running Shoes",
                    "amount": 150.0
                }
            }
            """;

        // Mock exchange behavior
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn(inputJson);

        // Call the method
        shoeProcessFlow.shoeProcessFlow(exchange);

        // Capture the modified body
        verify(message).setBody(bodyCaptor.capture());
        String updatedJson = bodyCaptor.getValue();

        assertNotNull(updatedJson);

        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode updatedOrder = mapper.readTree(updatedJson).at("/order");

        double updatedAmount = updatedOrder.get("amount").asDouble();

        // Check if the discount was applied (150 * 0.9 = 135)
        assertEquals(135.0, updatedAmount);
    }

    @Test
    void testShoeProcessFlow_NoDiscountApplied() throws Exception {
        // Input JSON where amount <= 100
        String inputJson = """
            {
                "order": {
                    "productId": "S124",
                    "productName": "Casual Shoes",
                    "amount": 90.0
                }
            }
            """;

        // Mock exchange behavior
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn(inputJson);

        // Call the method
        shoeProcessFlow.shoeProcessFlow(exchange);

        // Capture the modified body
        verify(message).setBody(bodyCaptor.capture());
        String updatedJson = bodyCaptor.getValue();

        assertNotNull(updatedJson);

        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode updatedOrder = mapper.readTree(updatedJson).at("/order");

        double updatedAmount = updatedOrder.get("amount").asDouble();

        // Check that no discount was applied
        assertEquals(90.0, updatedAmount);
    }

}
