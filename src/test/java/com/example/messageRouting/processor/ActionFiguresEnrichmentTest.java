package com.example.messageRouting.processor;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ActionFiguresEnrichmentTest extends CamelTestSupport {
	@InjectMocks
	private ActionFiguresEnrichment actionFiguresEnrichment;
	
	@Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @Captor
    private ArgumentCaptor<String> bodyCaptor;
    @BeforeEach
    void initMocks() {
        actionFiguresEnrichment.setCamelContext(new DefaultCamelContext());
    }
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> actionFiguresEnrichment.configure());
    }
    
    @Test
    void testActionFiguresEnrichment_success() throws Exception {
        // Sample JSON input
        String inputJson = """
            {
                "order": {
                    "productId": "12345",
                    "productName": "Action Figure"
                }
            }
            """;

        // Mock exchange behavior
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn(inputJson);

        // Call the method
        actionFiguresEnrichment.actionFiguresEnrichment(exchange);

        // Capture the modified body
        verify(message).setBody(bodyCaptor.capture());
        String updatedJson = bodyCaptor.getValue();

        // Validate updated JSON
        assertNotNull(updatedJson);

        ObjectMapper mapper = new ObjectMapper();
        String specialBadge = mapper.readTree(updatedJson)
                .at("/order/specialBadge")
                .asText();

        assertEquals("Limited Edition", specialBadge);
    }

}
