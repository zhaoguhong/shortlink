package com.zhaoguhong.shortlink.admin.generator;

import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 递增序列 + Base62 短链码生成器。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
@Component
public class Base62ShortCodeGenerator implements ShortCodeGenerator {

    private static final String REDIS_SEQUENCE_KEY = "shortlink:codegen:sequence";

    private final StringRedisTemplate stringRedisTemplate;

    public Base62ShortCodeGenerator(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ShortCodeGenerateStrategy strategy() {
        return ShortCodeGenerateStrategy.REDIS_BASE62;
    }

    @Override
    public String generate(String originalUrl, int retryCount) {
        Long sequenceValue = stringRedisTemplate.opsForValue().increment(REDIS_SEQUENCE_KEY);
        if (sequenceValue == null) {
            throw new IllegalStateException("Redis INCR 返回空值，无法生成短链码");
        }
        return Base62Codec.encode(sequenceValue);
    }
}
