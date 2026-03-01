package com.zhaoguhong.shortlink.admin.generator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorRouterTest {

    @Test
    void shouldRouteByConfiguredStrategy() {
        ShortCodeGenerator redisGenerator = new FixedValueGenerator(ShortCodeGenerateStrategy.REDIS_BASE62, "redis-code");
        ShortCodeGenerator murmurGenerator = new FixedValueGenerator(ShortCodeGenerateStrategy.MURMUR_HASH_BASE62, "murmur-code");

        ShortCodeGeneratorRouter router =
                new ShortCodeGeneratorRouter(List.of(redisGenerator, murmurGenerator), "murmur-hash-base62");

        assertThat(router.generate("https://example.com")).isEqualTo("murmur-code");
    }

    @Test
    void shouldFallbackToRedisWhenConfiguredStrategyIsInvalid() {
        ShortCodeGenerator redisGenerator = new FixedValueGenerator(ShortCodeGenerateStrategy.REDIS_BASE62, "redis-code");
        ShortCodeGenerator murmurGenerator = new FixedValueGenerator(ShortCodeGenerateStrategy.MURMUR_HASH_BASE62, "murmur-code");

        ShortCodeGeneratorRouter router =
                new ShortCodeGeneratorRouter(List.of(redisGenerator, murmurGenerator), "invalid-strategy");

        assertThat(router.generate("https://example.com")).isEqualTo("redis-code");
    }

    private static final class FixedValueGenerator implements ShortCodeGenerator {

        private final ShortCodeGenerateStrategy strategy;
        private final String code;

        private FixedValueGenerator(ShortCodeGenerateStrategy strategy, String code) {
            this.strategy = strategy;
            this.code = code;
        }

        @Override
        public ShortCodeGenerateStrategy strategy() {
            return strategy;
        }

        @Override
        public String generate(String originalUrl) {
            return code;
        }
    }
}
