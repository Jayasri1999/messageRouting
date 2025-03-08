package com.example.messageRouting.adapter.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.entity.CountryCrossRef;
import com.example.messageRouting.repository.CountryCrossRefRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CountryCrossRefCache {
	@Autowired
	private CountryCrossRefRepository countryCrossRefRepository;
    private final Map<String, CountryCrossRef> countryCrossRefCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public CountryCrossRefCache(CountryCrossRefRepository countryCrossRefRepository) {
        scheduler.scheduleAtFixedRate(this::refreshCache, 0, 1, TimeUnit.HOURS);
    }
    public CountryCrossRef getCountryCrossRefByKey(String id) {
    	String key = id;
        CountryCrossRef countryCrossRef=null;
		try {
			countryCrossRef = countryCrossRefCache.get(key);
			System.out.println("+++++++++++CountryCrosRef+++++++++++++++ "+countryCrossRef);
			if (countryCrossRef == null) {
			    // Fetch from DB if not in cache
				System.out.println("+++++++++++Data is not in cache+++++++++++++++ key:"+key);
				countryCrossRef = countryCrossRefRepository.findByKey(key)
						.orElseThrow(() -> new IllegalArgumentException("countryCrossRef not found"));
			    if (countryCrossRef != null) {
			    	countryCrossRefCache.put(key, countryCrossRef);
			    }
			    
			}
		} catch (IllegalArgumentException e) {
            log.error("Error: countryCrossRef not found. Key: {}", key, e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching countryCrossRef for key: {}", key, e);
        }
        return countryCrossRef;
    }
    
    private void refreshCache() {
        log.info("Refreshing countryCrossRefCache");
        countryCrossRefCache.clear();
    }
}
