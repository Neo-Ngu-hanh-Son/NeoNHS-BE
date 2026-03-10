package fpt.project.NeoNHS.tasks;

import fpt.project.NeoNHS.constants.BlogConstants;
import fpt.project.NeoNHS.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class BlogViewSyncTask {
    private final RedisTemplate<String, Object> redisTemplate;
    private final BlogService blogService;

    @Scheduled(fixedRate = 60000 * 15)
    @Transactional
    public void syncViewsToDb() {
        System.out.println("Starting BlogViewSyncTask at " + LocalDateTime.now());
        Set<String> keys = redisTemplate.keys(BlogConstants.BLOG_VIEW_COUNT_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        int processed = 0;
        for (String key : keys) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) continue;
            int viewsToAdd = Integer.parseInt(value.toString());
            String blogId = key.replace(BlogConstants.BLOG_VIEW_COUNT_KEY_PREFIX, "");
            blogService.addTotalViewCount(UUID.fromString(blogId), viewsToAdd);
            redisTemplate.delete(key);
            processed++;
        }

        System.out.println("Synced " + processed + " blog view counts to DB.");
    }
}
