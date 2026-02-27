package com.zhaoguhong.shortlink.admin.generator;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base62 短链码生成器。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@Component
public class Base62ShortCodeGenerator implements ShortCodeGenerator {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    @Override
    public String generate() {
        long value = sequence.incrementAndGet();
        StringBuilder builder = new StringBuilder();
        do {
            int index = (int) (value % BASE62_ALPHABET.length());
            builder.append(BASE62_ALPHABET.charAt(index));
            value /= BASE62_ALPHABET.length();
        } while (value > 0);
        return builder.reverse().toString();
    }
}
