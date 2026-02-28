package com.zhaoguhong.shortlink.server.service.impl;

import com.zhaoguhong.shortlink.server.entity.ShortLink;
import com.zhaoguhong.shortlink.server.mapper.ShortLinkMapper;
import com.zhaoguhong.shortlink.server.service.ShortLinkService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
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
    private static final Long NULL_LINK_ID = -1L;
    private static final String NULL_LINK_CODE = "__NULL__";
    private static final ShortLink NULL_LINK_MARKER = buildNullLinkMarker();
    private static final Duration UV_KEY_TTL = Duration.ofDays(7);
    private static final int LINK_STATUS_DISABLED = 0;

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, ShortLink> shortLinkRedisTemplate;
    /** 正常短链缓存TTL（秒），小于等于0表示禁用正常短链缓存。 */
    @Value("${shortlink.cache.code-ttl-seconds:1800}")
    private long codeCacheTtlSeconds;
    /** 空值缓存TTL（秒），小于等于0表示禁用空值缓存。 */
    @Value("${shortlink.cache.null-ttl-seconds:120}")
    private long codeCacheNullTtlSeconds;

    public ShortLinkServiceImpl(ShortLinkMapper shortLinkMapper,
                                StringRedisTemplate stringRedisTemplate,
                                RedisTemplate<String, ShortLink> shortLinkRedisTemplate) {
        this.shortLinkMapper = shortLinkMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.shortLinkRedisTemplate = shortLinkRedisTemplate;
    }

    @Override
    public ShortLink getByCode(String code) {
        String cacheKey = CODE_CACHE_PREFIX + code;
        ShortLink link = getLinkFromCache(code, cacheKey);
        if (link != null) {
            if (isNullLinkMarker(link)) {
                return null;
            }
            return isLinkValid(link) ? link : null;
        }
        link = shortLinkMapper.findByCode(code);
        if (isLinkValid(link)) {
            cacheLink(cacheKey, link);
            return link;
        } else {
            cacheNullLink(cacheKey);
            return null;
        }
    }

    private ShortLink getLinkFromCache(String code, String cacheKey) {
        try {
            return shortLinkRedisTemplate.opsForValue().get(cacheKey);
        } catch (SerializationException e) {
            log.warn("deserialize short link cache failed, code={}", code, e);
            try {
                shortLinkRedisTemplate.delete(cacheKey);
            } catch (Exception deleteException) {
                log.warn("delete corrupted short link cache failed, code={}", code, deleteException);
            }
            return null;
        } catch (Exception e) {
            log.warn("read short link cache failed, code={}", code, e);
            return null;
        }
    }

    private boolean isNullLinkMarker(ShortLink link) {
        return link != null && NULL_LINK_ID.equals(link.getId()) && NULL_LINK_CODE.equals(link.getCode());
    }

    private static ShortLink buildNullLinkMarker() {
        ShortLink marker = new ShortLink();
        marker.setId(NULL_LINK_ID);
        marker.setCode(NULL_LINK_CODE);
        return marker;
    }

    private void cacheNullLink(String cacheKey) {
        // 空值短缓存，降低恶意/高频不存在短码请求对数据库的穿透压力。
        if (codeCacheNullTtlSeconds > 0) {
            shortLinkRedisTemplate.opsForValue().set(
                    cacheKey,
                    NULL_LINK_MARKER,
                    Duration.ofSeconds(codeCacheNullTtlSeconds)
            );
        }
    }

    private void cacheLink(String cacheKey, ShortLink link) {
        if (codeCacheTtlSeconds <= 0) {
            return;
        }
        Duration ttl;
        if (link.getExpireTime() == null) {
            ttl = Duration.ofSeconds(codeCacheTtlSeconds);
        } else {
            LocalDateTime now = LocalDateTime.now();
            long secondsLeft = Duration.between(now, link.getExpireTime()).getSeconds();
            if (secondsLeft <= 0) {
                return;
            }
            ttl = Duration.ofSeconds(Math.min(secondsLeft, codeCacheTtlSeconds));
        }
        shortLinkRedisTemplate.opsForValue().set(cacheKey, link, ttl);
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

    private boolean isLinkValid(ShortLink link) {
        if (link == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (link.getStatus() != null && link.getStatus() == LINK_STATUS_DISABLED) {
            return false;
        }
        if (link.getExpireTime() != null && link.getExpireTime().isBefore(now)) {
            return false;
        }
        return true;
    }

}
