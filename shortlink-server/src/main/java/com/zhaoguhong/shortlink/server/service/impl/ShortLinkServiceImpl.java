package com.zhaoguhong.shortlink.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaoguhong.shortlink.common.exception.BizException;
import com.zhaoguhong.shortlink.server.entity.ShortLink;
import com.zhaoguhong.shortlink.server.mapper.ShortLinkMapper;
import com.zhaoguhong.shortlink.server.service.ShortLinkService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger log = LoggerFactory.getLogger(ShortLinkServiceImpl.class);

    private static final String CODE_CACHE_PREFIX = "shortlink:code:";
    private static final String CODE_CACHE_NULL_VALUE = "__NULL__";
    private static final Duration UV_KEY_TTL = Duration.ofDays(7);
    private static final int LINK_STATUS_DISABLED = 0;
    private static final int ERROR_CODE_INTERNAL = 500;

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    /** 正常短链缓存TTL（秒），小于等于0表示禁用正常短链缓存。 */
    @Value("${shortlink.cache.code-ttl-seconds:1800}")
    private long codeCacheTtlSeconds;
    /** 空值缓存TTL（秒），小于等于0表示禁用空值缓存。 */
    @Value("${shortlink.cache.null-ttl-seconds:120}")
    private long codeCacheNullTtlSeconds;

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
        ShortLink cachedLink = getLinkFromCache(code, cacheKey);
        if (cachedLink != null) {
            return cachedLink;
        }

        ShortLink link = shortLinkMapper.findByCode(code);
        link = validateAndGetValidLink(link);
        if (link == null) {
            // 空值短缓存，降低恶意/高频不存在短码请求对数据库的穿透压力。
            if (codeCacheNullTtlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(
                        cacheKey,
                        CODE_CACHE_NULL_VALUE,
                        Duration.ofSeconds(codeCacheNullTtlSeconds)
                );
            }
            return null;
        }

        Duration ttl = resolveCacheTtl(link);
        if (!ttl.isZero() && !ttl.isNegative()) {
            stringRedisTemplate.opsForValue().set(cacheKey, serializeLink(link), ttl);
        }
        return link;
    }

    private ShortLink getLinkFromCache(String code, String cacheKey) {
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (CODE_CACHE_NULL_VALUE.equals(cached) || StringUtils.isEmpty(cached)) {
            return null;
        }
        try {
            ShortLink cachedLink = objectMapper.readValue(cached, ShortLink.class);
            return validateAndGetValidLink(cachedLink);
        } catch (Exception e) {
            log.warn("deserialize short link cache failed, code={}", code, e);
            stringRedisTemplate.delete(cacheKey);
            return null;
        }
    }

    @Override
    public void recordAccess(Long linkId, String visitorKey) {
        if (linkId == null) {
            return;
        }
        String pvKey = "shortlink:stats:" + linkId + ":pv";
        stringRedisTemplate.opsForValue().increment(pvKey);

        if (StringUtils.isNotEmpty(visitorKey)) {
            String day = LocalDate.now().toString();
            String uvKey = "shortlink:uv:" + linkId + ":" + day;
            String hash = DigestUtils.md5DigestAsHex(visitorKey.getBytes(StandardCharsets.UTF_8));
            stringRedisTemplate.opsForSet().add(uvKey, hash);
            stringRedisTemplate.expire(uvKey, UV_KEY_TTL);
        }

        String recentKey = "shortlink:stats:" + linkId + ":recent_access_time";
        stringRedisTemplate.opsForValue().set(recentKey, LocalDateTime.now().toString());
    }

    private ShortLink validateAndGetValidLink(ShortLink link) {
        if (link == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (link.getStatus() != null && link.getStatus() == LINK_STATUS_DISABLED) {
            return null;
        }
        if (link.getExpireTime() != null && link.getExpireTime().isBefore(now)) {
            return null;
        }
        return link;
    }

    private Duration resolveCacheTtl(ShortLink link) {
        if (codeCacheTtlSeconds <= 0) {
            return Duration.ZERO;
        }
        long defaultSeconds = codeCacheTtlSeconds;
        if (link.getExpireTime() == null) {
            return Duration.ofSeconds(defaultSeconds);
        }
        LocalDateTime now = LocalDateTime.now();
        long secondsLeft = Duration.between(now, link.getExpireTime()).getSeconds();
        if (secondsLeft <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofSeconds(Math.min(secondsLeft, defaultSeconds));
    }

    private String serializeLink(ShortLink link) {
        try {
            return objectMapper.writeValueAsString(link);
        } catch (JsonProcessingException e) {
            throw new BizException(ERROR_CODE_INTERNAL, "短链缓存序列化失败", e);
        }
    }

}
