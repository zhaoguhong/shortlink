package com.zhaoguhong.shortlink.admin.generator;

/**
 * 短链码生成器接口。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
public interface ShortCodeGenerator {

    /**
     * 当前生成器支持的策略类型。
     *
     * @return 策略类型
     */
    ShortCodeGenerateStrategy strategy();

    /**
     * 生成短链码。
     *
     * @param originalUrl 原始链接
     * @return Base62 等编码后的短链码
     */
    String generate(String originalUrl);
}
