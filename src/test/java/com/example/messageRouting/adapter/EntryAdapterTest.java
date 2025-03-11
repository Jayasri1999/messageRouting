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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.service.ErrorLogsService;

@ExtendWith(MockitoExtension.class)
public class EntryAdapterTest extends CamelTestSupport{
	@InjectMocks
	EntryAdapter entryAdapter;
	
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
        entryAdapter.setCamelContext(new DefaultCamelContext());
    }
    
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> entryAdapter.configure());
    }
    
    @Test
    void testCbrProcess1() {
    	assertDoesNotThrow(() -> entryAdapter.entryProcess1(exchange));
    }
}
