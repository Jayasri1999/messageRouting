package com.example.messageRouting.adapter;

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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.service.ErrorLogsService;

@ExtendWith(MockitoExtension.class)
public class ActiveMQOutboundAdapterTest extends CamelTestSupport{
	
	@Mock
    private ProcessFlowCache processFlowCache;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ErrorLogsService errorLogsService;

    @InjectMocks
    private ActiveMQOutboundAdapter activeMQOutboundAdapter;
    
    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @Mock
    private CamelContext camelContext;

    @Mock
    private Language xpathLanguage;

    @Mock
    private Expression categoryExpr;

    @Mock
    private Expression subCategoryExpr;
    @Mock
    private ProcessFlow processFlow;
    
    @BeforeEach
    void initMocks() {
    	activeMQOutboundAdapter.setCamelContext(new DefaultCamelContext());
        exchange = new DefaultExchange(new DefaultCamelContext());
    }
    
    @Test
    public void testConfigure_withException() throws Exception {
        // There is no direct way to test the "configure" method of RouteBuilder without a CamelContext

        assertDoesNotThrow(() -> activeMQOutboundAdapter.configure());
    }
    
    @Test
    public void testOutboundProcess1() {
        exchange.getIn().setHeader("scenario", "TestScenario");
        exchange.getIn().setHeader("country", "US");
        exchange.getIn().setHeader("instance", 1);

        activeMQOutboundAdapter.outboundProcess1(exchange);

        String expectedQueue = "TestScenario.US.1.out";
        assertEquals(expectedQueue, exchange.getIn().getHeader("nextQueue"));
    }

}
