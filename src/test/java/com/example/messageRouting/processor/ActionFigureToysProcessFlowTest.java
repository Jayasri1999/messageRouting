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

import com.example.messageRouting.adapter.EntryAdapter;
import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.service.ErrorLogsService;
@ExtendWith(MockitoExtension.class)
public class ActionFigureToysProcessFlowTest extends CamelTestSupport{
	@InjectMocks
	ActionFigureToysProcessFlow actionFigureToysProcessFlow;
	
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
        actionFigureToysProcessFlow.setCamelContext(new DefaultCamelContext());
    }
    
    @Test
    void testConfigure() throws Exception {
        assertDoesNotThrow(()-> actionFigureToysProcessFlow.configure());
    }
    
    @Test
    void testactionFiguresProcessFlow() {
    	assertDoesNotThrow(() -> actionFigureToysProcessFlow.actionFiguresProcessFlow(exchange));
    }
}
