package com.zhaoguhong.shortlink.admin.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;

class RedisBase62ShortCodeGeneratorTest {

    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldGenerateBase62CodeFromRedisIncrement() {
        Mockito.when(valueOperations.increment("shortlink:codegen:sequence")).thenReturn(63L);

        RedisBase62ShortCodeGenerator generator = new RedisBase62ShortCodeGenerator(stringRedisTemplate);

        String result = generator.generate("https://example.com");

        assertThat(result).isEqualTo("11");
        assertThat(generator.strategy()).isEqualTo(ShortCodeGenerateStrategy.REDIS_BASE62);
    }
}
