package com.zhaoguhong.shortlink.admin.generator;

import com.zhaoguhong.shortlink.admin.config.ShortCodeGenerateProperties;
import org.apache.commons.codec.digest.MurmurHash3;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MurmurHash 短链码生成器。
 *
 * @author zhaoguhong
 * @date 2026/3/1
 */
@Component
public class MurmurHashShortCodeGenerator implements ShortCodeGenerator {

    private static final String PAD_CHAR = "0";
    private static final String NONCE_SEPARATOR = "@";

    private final ShortCodeGenerateProperties properties;

    public MurmurHashShortCodeGenerator(ShortCodeGenerateProperties properties) {
        this.properties = properties;
    }

    @Override
    public ShortCodeGenerateStrategy strategy() {
        return ShortCodeGenerateStrategy.MURMUR_HASH_BASE62;
    }

    @Override
    public String generate(String originalUrl) {
        String seedValue = buildSeedValue(originalUrl);
        int hashValue = MurmurHash3.hash32x86(seedValue.getBytes(StandardCharsets.UTF_8));
        String base62 = Base62Codec.encode(Integer.toUnsignedLong(hashValue));
        return normalizeLength(base62, properties.getMurmur().getLength());
    }

    private String buildSeedValue(String originalUrl) {
        String safeUrl = originalUrl == null ? "" : originalUrl;
        String nonce = UUID.randomUUID().toString();
        return safeUrl + NONCE_SEPARATOR + nonce;
    }

    private String normalizeLength(String input, int targetLength) {
        if (input.length() == targetLength) {
            return input;
        }
        if (input.length() > targetLength) {
            return input.substring(0, targetLength);
        }
        StringBuilder builder = new StringBuilder(targetLength);
        for (int i = input.length(); i < targetLength; i++) {
            builder.append(PAD_CHAR);
        }
        return builder.append(input).toString();
    }
}
