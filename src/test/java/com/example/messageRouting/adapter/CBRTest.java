package com.example.messageRouting.adapter;

import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.service.ErrorLogsService;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Language;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;

import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CBRTest extends CamelTestSupport{
	@InjectMocks
    private CBR cbr;

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
    
    @BeforeEach
    void initMocks() {
        cbr.setCamelContext(new DefaultCamelContext());
    }
    
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> cbr.configure());
    }
    
    @Test
    void testCbrProcess1() {
    	assertDoesNotThrow(() -> cbr.cbrProcess1(exchange));
    }

}
