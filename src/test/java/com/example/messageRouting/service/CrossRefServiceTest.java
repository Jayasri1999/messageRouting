package com.example.messageRouting.service;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Language;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.bson.json.JsonWriter.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.messageRouting.adapter.cache.CountryCrossRefCache;
import com.example.messageRouting.entity.CountryCrossRef;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CrossRefServiceTest {
	@InjectMocks
    private CrossRefService crossRefService;

    @Mock
    private CountryCrossRefCache countryCrossRefCache;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @Captor
    private ArgumentCaptor<String> updatedXmlCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcess_ReplacesValueInXml() throws Exception {
        String inputXml = "<root><countryCode>US</countryCode><countryName></countryName></root>";
        String keyXPath = "/root/countryCode";
        String replaceXPath = "/root/countryName";
        String refCollection = "countryCrossRef";
        String keyValue = "US";

        CountryCrossRef countryCrossRef = new CountryCrossRef();
        countryCrossRef.setKey("US");
        countryCrossRef.setValue("United States");

        // Mock exchange and message
        when(exchange.getIn()).thenReturn(message);
        when(exchange.getContext()).thenReturn(new DefaultCamelContext());

        // Mock headers
        when(message.getHeader("keyXPath", String.class)).thenReturn(keyXPath);
        when(message.getHeader("replaceXPath", String.class)).thenReturn(replaceXPath);
        when(message.getHeader("refCollection", String.class)).thenReturn(refCollection);

        // Mock body
        when(message.getBody(String.class)).thenReturn(inputXml);

        // Mock cache lookup
        when(countryCrossRefCache.getCountryCrossRefByKey(keyValue)).thenReturn(countryCrossRef);

        // Mock XPath evaluation
        DefaultCamelContext camelContext = new DefaultCamelContext();
        Language xpathLanguage = camelContext.resolveLanguage("xpath");

        // Instead of trying to mock deeply chained calls, use a spy or doReturn
        var spyContext = spy(camelContext);
        var spyLang = spy(xpathLanguage);
        var spyExpression = spy(xpathLanguage.createExpression(keyXPath + "/text()"));

        doReturn(spyLang).when(spyContext).resolveLanguage("xpath");
        doReturn(spyExpression).when(spyLang).createExpression(keyXPath + "/text()");
        doReturn(keyValue).when(spyExpression).evaluate(exchange, String.class);

        when(exchange.getContext()).thenReturn(spyContext);

        // Call the method under test
        crossRefService.process(exchange);

        // Verify that setBody was called with the expected XML
        verify(message, times(1)).setBody(updatedXmlCaptor.capture());
        String updatedXml = updatedXmlCaptor.getValue();

        // Assert the new XML contains the updated countryName
        assertTrue(updatedXml.contains("<countryName>United States</countryName>"));
    }
}
