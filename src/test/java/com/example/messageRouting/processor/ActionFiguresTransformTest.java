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

import com.example.messageRouting.adapter.TransformAdapter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.xml.transform.TransformerException;

@ExtendWith(MockitoExtension.class)
public class ActionFiguresTransformTest extends CamelTestSupport{
	@InjectMocks
    private ActionFiguresTransform actionFiguresTransform;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;
    @Captor
    private ArgumentCaptor<String> bodyCaptor;
    
    @BeforeEach
    void initMocks() {
        actionFiguresTransform.setCamelContext(new DefaultCamelContext());
    }
    
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> actionFiguresTransform.configure());
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

        String result = actionFiguresTransform.xmlToJsonTransformXSLT(xmlInput, xsltContent);

        assertNotNull(result);
        assertEquals("Success", result.trim());
    }
    
    @Test
    void testXmlToJsonTransformXSLT_invalidXslt() {
        String xmlInput = "<root><element>value</element></root>";
        String invalidXsltContent = "<invalid>xslt</invalid>";

        assertThrows(TransformerException.class, () -> {
            actionFiguresTransform.xmlToJsonTransformXSLT(xmlInput, invalidXsltContent);
        });
    }
    
    @Test
    void testactionFiguresTransform_success() throws Exception {
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

        actionFiguresTransform.actionFiguresTransform(exchange);

        // Capture the transformed body set on the message
        verify(message).setBody(bodyCaptor.capture());
        String transformedBody = bodyCaptor.getValue();

        assertNotNull(transformedBody);
        assertEquals("Success", transformedBody.trim()); // Match what the XSLT does
    }

}
