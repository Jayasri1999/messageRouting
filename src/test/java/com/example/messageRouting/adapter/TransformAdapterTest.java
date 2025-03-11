package com.example.messageRouting.adapter;

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

import javax.xml.transform.TransformerException;



import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.service.ErrorLogsService;

@ExtendWith(MockitoExtension.class)
public class TransformAdapterTest extends CamelTestSupport {
	@InjectMocks
    private TransformAdapter transformAdapter;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;
    @Captor
    private ArgumentCaptor<String> bodyCaptor;
    
    @BeforeEach
    void initMocks() {
        transformAdapter.setCamelContext(new DefaultCamelContext());
    }
    
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> transformAdapter.configure());
    }
    
    @Test
    void testXmlToJsonTransformXSLT_success() throws Exception {
        String xmlInput = "<root><element>value</element></root>";
        String xsltContent =
                "<?xml version=\"1.0\"?>\n" +
                "<xsl:stylesheet version=\"1.0\"\n" +
                "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "<xsl:output method=\"text\"/>\n" +
                "<xsl:template match=\"/\">\n" +
                "Success\n" +
                "</xsl:template>\n" +
                "</xsl:stylesheet>";

        String result = transformAdapter.xmlToJsonTransformXSLT(xmlInput, xsltContent);

        assertNotNull(result);
        assertEquals("Success", result.trim());
    }
    
    @Test
    void testXmlToJsonTransformXSLT_invalidXslt() {
        String xmlInput = "<root><element>value</element></root>";
        String invalidXsltContent = "<invalid>xslt</invalid>";

        assertThrows(TransformerException.class, () -> {
            transformAdapter.xmlToJsonTransformXSLT(xmlInput, invalidXsltContent);
        });
    }
    
    @Test
    void testTransformProcess1_success() throws Exception {
        String xmlInput = "<root><element>value</element></root>";
        String xsltContent =
                "<?xml version=\"1.0\"?>\n" +
                "<xsl:stylesheet version=\"1.0\"\n" +
                "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "<xsl:output method=\"text\"/>\n" +
                "<xsl:template match=\"/\">\n" +
                "Success\n" +
                "</xsl:template>\n" +
                "</xsl:stylesheet>";

        // Mock headers and body
        when(exchange.getIn()).thenReturn(message);
        when(message.getHeader("xsltContent", String.class)).thenReturn(xsltContent);
        when(message.getBody(String.class)).thenReturn(xmlInput);

        transformAdapter.transformProcess1(exchange);

        // Capture the transformed body set on the message
        verify(message).setBody(bodyCaptor.capture());
        String transformedBody = bodyCaptor.getValue();

        assertNotNull(transformedBody);
        assertEquals("Success", transformedBody.trim()); // Match what the XSLT does
    }

    
    

}
