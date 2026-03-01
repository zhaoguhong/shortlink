package com.zhaoguhong.shortlink.admin.config;

import com.zhaoguhong.shortlink.admin.generator.ShortCodeGenerateStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短链码生成配置。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
@ConfigurationProperties(prefix = "shortlink.codegen")
public class ShortCodeGenerateProperties {

    /**
     * 全局默认生成策略。
     */
    private ShortCodeGenerateStrategy strategy = ShortCodeGenerateStrategy.REDIS_BASE62;

    /**
     * MurmurHash 策略配置。
     */
    private final Murmur murmur = new Murmur();

    public ShortCodeGenerateStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ShortCodeGenerateStrategy strategy) {
        this.strategy = strategy;
    }

    public Murmur getMurmur() {
        return murmur;
    }

    /**
     * MurmurHash 生成器配置。
     */
    public static class Murmur {

        /**
         * 短码目标长度。
         */
        private int length = 8;

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            if (length <= 0) {
                throw new IllegalArgumentException("shortlink.codegen.murmur.length 必须大于 0");
            }
            this.length = length;
        }
    }
}
