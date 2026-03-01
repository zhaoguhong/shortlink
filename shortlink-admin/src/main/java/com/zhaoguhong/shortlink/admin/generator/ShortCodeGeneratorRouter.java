package com.zhaoguhong.shortlink.admin.generator;

import com.zhaoguhong.shortlink.admin.config.ShortCodeGenerateProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 短链码生成器路由器。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
@Component
public class ShortCodeGeneratorRouter {

    private final Map<ShortCodeGenerateStrategy, ShortCodeGenerator> generatorMap;
    private final ShortCodeGenerateProperties properties;

    public ShortCodeGeneratorRouter(List<ShortCodeGenerator> generators,
                                    ShortCodeGenerateProperties properties) {
        this.properties = properties;
        this.generatorMap = new EnumMap<>(ShortCodeGenerateStrategy.class);
        for (ShortCodeGenerator generator : generators) {
            this.generatorMap.put(generator.strategy(), generator);
        }
    }

    /**
     * 按全局配置路由到对应策略并生成短码。
     *
     * @param originalUrl 原始链接
     * @param retryCount 冲突重试次数
     * @return 短链码
     */
    public String generate(String originalUrl, int retryCount) {
        ShortCodeGenerateStrategy strategy = properties.getStrategy();
        ShortCodeGenerator generator = generatorMap.get(strategy);
        if (generator == null) {
            throw new IllegalStateException("未找到短链生成策略实现: " + strategy);
        }
        return generator.generate(originalUrl, retryCount);
    }
}
