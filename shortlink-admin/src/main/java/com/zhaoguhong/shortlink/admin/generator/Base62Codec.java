package com.zhaoguhong.shortlink.admin.generator;

/**
 * Base62 编解码工具（当前仅编码）。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
public final class Base62Codec {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int RADIX = BASE62_ALPHABET.length();

    private Base62Codec() {
    }

    /**
     * 将非负整数编码为 Base62 字符串。
     *
     * @param value 非负整数
     * @return Base62 字符串
     */
    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value 不能为负数");
        }
        if (value == 0) {
            return String.valueOf(BASE62_ALPHABET.charAt(0));
        }
        StringBuilder builder = new StringBuilder();
        long current = value;
        while (current > 0) {
            int index = (int) (current % RADIX);
            builder.append(BASE62_ALPHABET.charAt(index));
            current /= RADIX;
        }
        return builder.reverse().toString();
    }
}
