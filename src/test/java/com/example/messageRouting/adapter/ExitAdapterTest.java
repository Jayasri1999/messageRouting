package com.example.messageRouting.adapter;

import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.service.ErrorLogsService;


@ExtendWith(MockitoExtension.class)
public class ExitAdapterTest extends CamelTestSupport{
	 @InjectMocks
	    private ExitAdapter exitAdapter;

	    @Mock
	    private ProcessFlowCache processFlowCache;

	    @Mock
	    private ErrorLogsService errorLogsService;
	    @Mock
	    private Exchange exchange;
	    @Mock
	    private Message inMessage;
	    
	    @BeforeEach
	    void init()  {
	        exitAdapter.setCamelContext(new DefaultCamelContext());
	    }
	    
	    @Test
	    void testExitProcess1_logsInfo() {
	        assertDoesNotThrow(() -> exitAdapter.exitProcess1(exchange));
	    }
	    
	    @Test
	    void testConfigure() throws Exception {
	        assertDoesNotThrow(()-> exitAdapter.configure());
	    }
	    
	    
}
