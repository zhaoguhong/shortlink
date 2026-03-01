package com.zhaoguhong.shortlink.admin.generator;

import com.zhaoguhong.shortlink.admin.config.ShortCodeGenerateProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MurmurHashShortCodeGeneratorTest {

    @Test
    void shouldGenerateFixedLengthCodeAndVaryAcrossInvocations() {
        ShortCodeGenerateProperties properties = new ShortCodeGenerateProperties();
        properties.getMurmur().setLength(8);
        MurmurHashShortCodeGenerator generator = new MurmurHashShortCodeGenerator(properties);

        String first = generator.generate("https://example.com/path");
        String second = generator.generate("https://example.com/path");

        assertThat(first).hasSize(8);
        assertThat(second).hasSize(8);
        assertThat(second).isNotEqualTo(first);
        assertThat(generator.strategy()).isEqualTo(ShortCodeGenerateStrategy.MURMUR_HASH_BASE62);
    }

    @Test
    void shouldGenerateDifferentCodeForSameUrlAcrossMultipleInvocations() {
        ShortCodeGenerateProperties properties = new ShortCodeGenerateProperties();
        properties.getMurmur().setLength(8);
        MurmurHashShortCodeGenerator generator = new MurmurHashShortCodeGenerator(properties);

        String first = generator.generate("https://example.com/path");
        String second = generator.generate("https://example.com/path");

        assertThat(first).hasSize(8);
        assertThat(second).hasSize(8);
        assertThat(second).isNotEqualTo(first);
    }
}
