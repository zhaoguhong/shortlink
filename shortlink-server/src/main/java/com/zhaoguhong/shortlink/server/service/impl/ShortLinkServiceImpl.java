package com.zhaoguhong.shortlink.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaoguhong.shortlink.common.exception.BizException;
import com.zhaoguhong.shortlink.server.entity.ShortLink;
import com.zhaoguhong.shortlink.server.mapper.ShortLinkMapper;
import com.zhaoguhong.shortlink.server.service.ShortLinkService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 短链查询与访问统计服务实现。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@Service
public class ShortLinkServiceImpl implements ShortLinkService {

    private static final String CODE_CACHE_PREFIX = "shortlink:code:";
    private static final Duration CODE_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration UV_KEY_TTL = Duration.ofDays(7);

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public ShortLinkServiceImpl(ShortLinkMapper shortLinkMapper,
                                StringRedisTemplate stringRedisTemplate,
                                ObjectMapper objectMapper) {
        this.shortLinkMapper = shortLinkMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ShortLink getByCode(String code) {
        String cacheKey = CODE_CACHE_PREFIX + code;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            ShortLink cachedLink = deserializeLink(cached, cacheKey);
            if (cachedLink != null) {
                validateLink(cachedLink, true, cacheKey);
                return cachedLink;
            }
        }

        ShortLink link = shortLinkMapper.findByCode(code);
        validateLink(link, false, cacheKey);

        Duration ttl = resolveCacheTtl(link);
        if (!ttl.isZero() && !ttl.isNegative()) {
            stringRedisTemplate.opsForValue().set(cacheKey, serializeLink(link), ttl);
        }
        return link;
    }

    @Override
    public void recordAccess(Long linkId, String visitorKey) {
        if (linkId == null) {
            return;
        }
        String pvKey = "shortlink:stats:" + linkId + ":pv";
        stringRedisTemplate.opsForValue().increment(pvKey);

        if (visitorKey != null && !visitorKey.isEmpty()) {
            String day = LocalDate.now().toString();
            String uvKey = "shortlink:uv:" + linkId + ":" + day;
            String hash = DigestUtils.md5DigestAsHex(visitorKey.getBytes(StandardCharsets.UTF_8));
            stringRedisTemplate.opsForSet().add(uvKey, hash);
            stringRedisTemplate.expire(uvKey, UV_KEY_TTL);
        }

        String recentKey = "shortlink:stats:" + linkId + ":recent_access_time";
        stringRedisTemplate.opsForValue().set(recentKey, LocalDateTime.now().toString());
    }

    private void validateLink(ShortLink link, boolean fromCache, String cacheKey) {
        if (link == null) {
            throw new BizException(404, "短链不存在");
        }
        if (link.getStatus() != null && link.getStatus() == 0) {
            throw new BizException(403, "短链已被禁用");
        }
        if (link.getExpireTime() != null && link.getExpireTime().isBefore(LocalDateTime.now())) {
            if (fromCache) {
                stringRedisTemplate.delete(cacheKey);
            }
            throw new BizException(410, "短链已过期");
        }
    }

    private Duration resolveCacheTtl(ShortLink link) {
        if (link.getExpireTime() == null) {
            return CODE_CACHE_TTL;
        }
        long secondsLeft = Duration.between(LocalDateTime.now(), link.getExpireTime()).getSeconds();
        if (secondsLeft <= 0) {
            return Duration.ZERO;
        }
        long defaultSeconds = CODE_CACHE_TTL.getSeconds();
        return Duration.ofSeconds(Math.min(secondsLeft, defaultSeconds));
    }

    private String serializeLink(ShortLink link) {
        try {
            return objectMapper.writeValueAsString(link);
        } catch (JsonProcessingException e) {
            throw new BizException(500, "短链缓存序列化失败");
        }
    }

    private ShortLink deserializeLink(String cached, String cacheKey) {
        try {
            return objectMapper.readValue(cached, ShortLink.class);
        } catch (Exception e) {
            stringRedisTemplate.delete(cacheKey);
            return null;
        }
    }
}
