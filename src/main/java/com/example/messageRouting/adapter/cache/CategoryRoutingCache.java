package com.example.messageRouting.adapter.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.messageRouting.entity.CategoryRouting;
import com.example.messageRouting.repository.CategoryRoutingRepository;




@Component
public class CategoryRoutingCache {
	@Autowired
	private CategoryRoutingRepository categoryRoutingRepository;
    private final Map<String, CategoryRouting> categoryRoutingCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public CategoryRoutingCache() {
        // Schedule cache refresh every 1 hour
        scheduler.scheduleAtFixedRate(this::refreshCache, 0, 1, TimeUnit.HOURS);
    }
    public CategoryRouting getCategoryRouting(String category_name, String subcategory_name) {
        String key = generateKey(category_name, subcategory_name);
        CategoryRouting categoryRouting = categoryRoutingCache.get(key);
        System.out.println("+++++++++++categoryRouting+++++++++++++++ "+categoryRouting);
        if (categoryRouting == null) {
            // Fetch from DB if not in cache
        	System.out.println("+++++++++++Data is not in cache+++++++++++++++ "+category_name+subcategory_name);
        	categoryRouting = categoryRoutingRepository.findByCategoryNameAndSubcategoryName(category_name, subcategory_name)
    				.orElseThrow(() -> new IllegalArgumentException("Routing not found"));
            if (categoryRouting != null) {
            	categoryRoutingCache.put(key, categoryRouting);
            }
            
        }

        return categoryRouting;
    }

    private void refreshCache() {
    	categoryRoutingCache.clear();
    }
    private static String generateKey(String category_name, String subcategory_name) {
        return category_name + "." + subcategory_name;
    }
}
