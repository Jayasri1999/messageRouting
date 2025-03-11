package com.example.messageRouting.adapter;

import com.example.messageRouting.adapter.cache.ProcessFlowCache;
import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.entity.ProcessFlow.Hop;
import com.example.messageRouting.service.DecodeService;
import com.example.messageRouting.service.ErrorLogsService;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.Language;
import org.apache.camel.test.junit5.CamelTestSupport;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveMQInboundAdapterTest extends CamelTestSupport {

    @Mock
    private ProcessFlowCache processFlowCache;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ErrorLogsService errorLogsService;

    @InjectMocks
    private ActiveMQInboundAdapter activeMQInboundAdapter;
    
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
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConfigure() throws Exception {
        // Arrange
//        List<String> idsList = Arrays.asList("\"_id\": \"sc1.mx.1\"", "\"_id\": \"sc1.us.2\"");
    	List<String> idsList =  Arrays.asList(
                "{\"_id\": \"sc1.mx.1\"}",
                "{\"_id\": \"sc1.us.2\"}"
            );
        when(processFlowCache.getProcessFlowIdsByHopKey(anyString())).thenReturn(idsList);

        // Act
        activeMQInboundAdapter.configure();

        // Assert
        verify(processFlowCache, times(1)).getProcessFlowIdsByHopKey("ActiveMQInboundAdapter");
    }

  @Test
  void testConfigureInboundRoute_ShouldExecuteWithoutExceptions() {
      String processFlowId = "sc1.us.2";

      // We can't verify route configuration without Camel context running, but no exceptions is good
      assertDoesNotThrow(() ->  activeMQInboundAdapter.configureInboundRoute(processFlowId));
  }


  @Test
  void testInboundProcess1() throws Exception {
      // Setup
      String processFlowId = "pf123";
      String categoryName = "Electronics";
      String subCategoryName = "Mobile";

      // Mock Exchange & Message
      when(exchange.getIn()).thenReturn(message);
      when(exchange.getContext()).thenReturn(camelContext);
      when(message.getHeader("processFlowId", String.class)).thenReturn(processFlowId);

      // Mock XPath Language behavior
      when(camelContext.resolveLanguage("xpath")).thenReturn(xpathLanguage);

      when(xpathLanguage.createExpression("/order/category/name/text()")).thenReturn(categoryExpr);
      when(xpathLanguage.createExpression("/order/category/subcategories/subcategory/name/text()")).thenReturn(subCategoryExpr);

      when(categoryExpr.evaluate(exchange, String.class)).thenReturn(categoryName);
      when(subCategoryExpr.evaluate(exchange, String.class)).thenReturn(subCategoryName);

      // Mock ProcessFlow
      when(processFlowCache.getProcessFlowById(processFlowId)).thenReturn(processFlow);

      when(processFlow.getCountry()).thenReturn("US");
      when(processFlow.getScenario()).thenReturn("Scenario1");
      when(processFlow.getInstance()).thenReturn(1);

      // Mock Hops
      Hop activeMQHop = new Hop();
      activeMQHop.setNextHop("NextHop1");

      Hop nextHop = new Hop();
      nextHop.setInputQueue("NextQueue1");

      Map<String, Hop> hops = new HashMap<>();
      hops.put("ActiveMQInboundAdapter", activeMQHop);
      hops.put("NextHop1", nextHop);

      when(processFlow.getHops()).thenReturn(hops);

      // Invoke the method under test
      activeMQInboundAdapter.inboundProcess1(exchange);

      // Verify headers are set as expected
      verify(message).setHeader("scenario", "Scenario1");
      verify(message).setHeader("country", "US");
      verify(message).setHeader("instance", 1);
      verify(message).setHeader("category", categoryName);
      verify(message).setHeader("subCategory", subCategoryName);
      verify(message).setHeader("nextHop", "NextHop1");
      verify(message).setHeader("nextQueue", "NextQueue1");
  }
  
  
   


//
    @Test
    void testInvokeMethod() throws Exception {
    	ActiveMQInboundAdapter activeMQInboundAdapter = Mockito.spy(new ActiveMQInboundAdapter());
        activeMQInboundAdapter.invokeMethod("inboundProcess1", exchange);
        verify(activeMQInboundAdapter).inboundProcess1(exchange);
    }

    @Test
    void testInvokeClassMethod() throws Exception {
        String className = "com.example.messageRouting.adapter.ActiveMQInboundAdapterTest$DummyService";
        String methodName = "testMethod";
        DummyService dummyService = Mockito.spy(new DummyService());
     // Cast the class to Class<DummyService> to avoid type issues
        @SuppressWarnings("unchecked")
        Class<DummyService> clazz = (Class<DummyService>) Class.forName(className);
        when(applicationContext.getBean(clazz)).thenReturn(dummyService);
        activeMQInboundAdapter.invokeClassMethod(className, methodName, exchange);
        verify(dummyService).testMethod(exchange);
    }
    
    public class DummyService {
        public void testMethod(Exchange exchange) {
            System.out.println("DummyService invoked!");
        }
    }
}