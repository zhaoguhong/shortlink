package com.zhaoguhong.shortlink.admin.generator;

import com.zhaoguhong.shortlink.admin.config.ShortCodeGenerateProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorRouterTest {

    @Test
    void shouldRouteByConfiguredStrategy() {
        ShortCodeGenerateProperties properties = new ShortCodeGenerateProperties();
        properties.setStrategy(ShortCodeGenerateStrategy.MURMUR_HASH_BASE62);

        ShortCodeGenerator redisGenerator = new FixedValueGenerator(ShortCodeGenerateStrategy.REDIS_BASE62, "redis-code");
        ShortCodeGenerator murmurGenerator = new FixedValueGenerator(ShortCodeGenerateStrategy.MURMUR_HASH_BASE62, "murmur-code");

        ShortCodeGeneratorRouter router = new ShortCodeGeneratorRouter(List.of(redisGenerator, murmurGenerator), properties);

        assertThat(router.generate("https://example.com", 0)).isEqualTo("murmur-code");
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
        public String generate(String originalUrl, int retryCount) {
            return code;
        }
    }
}
