package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.service.RedisBlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static fpt.project.NeoNHS.constants.BlogConstants.BLOG_VIEW_COUNT_KEY_PREFIX;

@Service
@RequiredArgsConstructor
public class RedisBlogServiceImpl implements RedisBlogService {
    private final StringRedisTemplate redis;


    @Override
    public void incrementTempViewCount(UUID id) {
        redis.opsForValue().increment(BLOG_VIEW_COUNT_KEY_PREFIX + id, 1);
    }
}
