package com.example.messageRouting.adapter.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.entity.ScenarioCrossRef;
import com.example.messageRouting.repository.ScenarioCrossRefRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScenarioCrossRefCache {
	@Autowired
	private ScenarioCrossRefRepository scenarioCrossRefRepository;
    private final Map<String, ScenarioCrossRef> scenarioCrossRefCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public ScenarioCrossRefCache() {
    	// Schedule cache refresh every 1 hour
        scheduler.scheduleAtFixedRate(this::refreshCache, 0, 1, TimeUnit.HOURS);
	}
    
    public ScenarioCrossRef getScenarioCrossRefById(String id) {
    	String key = id;
    	ScenarioCrossRef scenarioCrossRef = scenarioCrossRefCache.get(key);
        System.out.println("+++++++++++Process Flow+++++++++++++++ "+scenarioCrossRef);
        if (scenarioCrossRef == null) {
            // Fetch from DB if not in cache
        	System.out.println("+++++++++++Data is not in cache+++++++++++++++ key:"+key);
        	scenarioCrossRef = scenarioCrossRefRepository.findById(key)
    				.orElseThrow(() -> new IllegalArgumentException("scenarioCrossRef not found"));
            if (scenarioCrossRef != null) {
            	scenarioCrossRefCache.put(key, scenarioCrossRef);
            }
            
        }
        return scenarioCrossRef;
    }
    
    private void refreshCache() {
        log.info("Refreshing scenarioCrossRefCache");
        scenarioCrossRefCache.clear();
    }
}
