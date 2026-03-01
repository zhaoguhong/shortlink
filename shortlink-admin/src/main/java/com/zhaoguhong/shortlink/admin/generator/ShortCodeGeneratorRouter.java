package com.zhaoguhong.shortlink.admin.generator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * 短链码生成器路由器。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
@Component
public class ShortCodeGeneratorRouter {

    private static final Logger log = LoggerFactory.getLogger(ShortCodeGeneratorRouter.class);
    private final ShortCodeGenerator selectedGenerator;

    public ShortCodeGeneratorRouter(List<ShortCodeGenerator> generators,
                                    @Value("${shortlink.codegen.strategy:redis-base62}") String strategy) {
        ShortCodeGenerateStrategy selectedStrategy = resolveStrategy(strategy);
        this.selectedGenerator = generators.stream()
                .filter(generator -> generator.strategy() == selectedStrategy)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到短链生成策略实现: " + selectedStrategy));
    }

    /**
     * 按全局配置路由到对应策略并生成短码。
     *
     * @param originalUrl 原始链接
     * @return 短链码
     */
    public String generate(String originalUrl) {
        return selectedGenerator.generate(originalUrl);
    }

    private ShortCodeGenerateStrategy resolveStrategy(String strategyValue) {
        if (StringUtils.isBlank(strategyValue)) {
            return ShortCodeGenerateStrategy.REDIS_BASE62;
        }
        String normalized = strategyValue.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "redis-base62" -> ShortCodeGenerateStrategy.REDIS_BASE62;
            case "murmur-hash-base62" -> ShortCodeGenerateStrategy.MURMUR_HASH_BASE62;
            default -> {
                log.error("短链生成策略配置非法: {}，已回退默认策略 redis-base62", strategyValue);
                yield ShortCodeGenerateStrategy.REDIS_BASE62;
            }
        };
    }
}
