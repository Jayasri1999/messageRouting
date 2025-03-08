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

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProcessFlowCache {

    @Autowired
    private ProcessFlowRepository processFlowRepository;

    private final Map<String, ProcessFlow> processFlowCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> hopKeyIdsCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ProcessFlowCache() {
        // Schedule cache refresh every 1 hour
        scheduler.scheduleAtFixedRate(this::refreshCache, 0, 1, TimeUnit.HOURS);
    }

    public ProcessFlow getProcessFlowById(String id) {
    	String key = id;
        ProcessFlow processFlow=null;
		try {
			processFlow = processFlowCache.get(key);
			System.out.println("+++++++++++Process Flow+++++++++++++++ "+processFlow);
			if (processFlow == null) {
			    // Fetch from DB if not in cache
				System.out.println("+++++++++++Data is not in cache+++++++++++++++ key:"+key);
				processFlow = processFlowRepository.findById(key)
						.orElseThrow(() -> new IllegalArgumentException("ProcessFlow not found"));
			    if (processFlow != null) {
			    	processFlowCache.put(key, processFlow);
			    }
			    
			}
		} catch (IllegalArgumentException e) {
            log.error("Error: processFlow not found. Key: {}", key, e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching processfFlow for key: {}", key, e);
        }
        return processFlow;
    }


    public List<String> getProcessFlowIdsByHopKey(String hopKey) {
    	String key=hopKey;
    	List<String> ids = null;
		try {
			ids = hopKeyIdsCache.get(key);
			System.out.println("+++++++++++Get Process Flow Ids+++++++++++++++ ");
			if (ids == null) {
			    // Fetch from DB if not in cache
				System.out.println("+++++++++++Ids Data is not in cache+++++++++++++++ ");
				ids = processFlowRepository.findAllIdsByHopKey(key);
			    if (ids != null) {
			    	hopKeyIdsCache.put(key, ids);
			    	System.out.println("+++++++++++Ids are fetched+++++++++++++++ "+ids.size());
			    }else {
			    	System.out.println("+++++++++++Ids are not fetched from db+++++++++++++++ ");
			    }
			    
			}
		} catch (IllegalArgumentException e) {
            log.error("Error: processFlow not found for HopKey. Key: {}", key, e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching processFlow for Hopkey: {}", key, e);
        }
        return ids;
    }


    private void refreshCache() {
        log.info("Refreshing processFlowCache and hopKeyIdsCache...");
        processFlowCache.clear();
        hopKeyIdsCache.clear();
    }
}
