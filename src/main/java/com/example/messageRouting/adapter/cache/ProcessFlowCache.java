package com.example.messageRouting.adapter.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.entity.ProcessFlow;
import com.example.messageRouting.repository.ProcessFlowRepository;

import lombok.extern.java.Log;


@Component
public class ProcessFlowCache {
	@Autowired
	private ProcessFlowRepository processFlowRepository;

    private final Map<String, ProcessFlow> cache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> idsCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public ProcessFlowCache() {
        // Schedule cache refresh every 1 hour
        scheduler.scheduleAtFixedRate(this::refreshCache, 0, 1, TimeUnit.HOURS);
    }

    public ProcessFlow getProcessFlowById(String id) {
        String key = id;
        ProcessFlow processFlow = cache.get(key);
        System.out.println("+++++++++++Process Flow+++++++++++++++ "+processFlow);
        if (processFlow == null) {
            // Fetch from DB if not in cache
        	System.out.println("+++++++++++Data is not in cache+++++++++++++++ key:"+key);
        	processFlow = processFlowRepository.findById(key)
    				.orElseThrow(() -> new IllegalArgumentException("ProcessFlow not found"));
            if (processFlow != null) {
                cache.put(key, processFlow);
            }
            
        }

        return processFlow;
    }
    
    public List<String>  getProcessFlowIds() {
        String key = "1";
        List<String> idsList= idsCache.get(key);
        System.out.println("+++++++++++Get Process Flow Ids+++++++++++++++ ");
        if (idsList == null) {
            // Fetch from DB if not in cache
        	System.out.println("+++++++++++Ids Data is not in cache+++++++++++++++ ");
        	idsList = processFlowRepository.findAllIds();
            if (idsList != null) {
            	idsCache.put(key, idsList);
            	System.out.println("+++++++++++Ids are fetched+++++++++++++++ "+idsList.size());
            }else {
            	System.out.println("+++++++++++Ids are not fetched from db+++++++++++++++ ");
            }
            
        }

        return idsList;
    }

    private void refreshCache() {
        cache.clear();
    }

}
