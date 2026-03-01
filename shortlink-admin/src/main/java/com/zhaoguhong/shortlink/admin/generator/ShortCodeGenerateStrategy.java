package com.zhaoguhong.shortlink.admin.generator;

/**
 * 短链码生成策略枚举。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
public enum ShortCodeGenerateStrategy {

    /**
     * Redis 递增序列 + Base62 编码。
     */
    REDIS_BASE62,

    /**
     * 基于 MurmurHash 的固定长度短码。
     */
    MURMUR_HASH
}
