package com.zhaoguhong.shortlink.admin.generator;

/**
 * 短链码生成器接口。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
public interface ShortCodeGenerator {

    /**
     * 生成短链码。
     *
     * @return Base62 等编码后的短链码
     */
    String generate();
}
