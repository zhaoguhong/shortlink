package com.zhaoguhong.shortlink.admin.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MurmurHashShortCodeGeneratorTest {

    @Test
    void shouldGenerateFixedLengthCodeAndVaryAcrossInvocations() {
        MurmurHashShortCodeGenerator generator = new MurmurHashShortCodeGenerator();

        String first = generator.generate("https://example.com/path");
        String second = generator.generate("https://example.com/path");

        assertThat(first).hasSize(6);
        assertThat(second).hasSize(6);
        assertThat(second).isNotEqualTo(first);
        assertThat(generator.strategy()).isEqualTo(ShortCodeGenerateStrategy.MURMUR_HASH_BASE62);
    }

    @Test
    void shouldGenerateDifferentCodeForSameUrlAcrossMultipleInvocations() {
        MurmurHashShortCodeGenerator generator = new MurmurHashShortCodeGenerator();

        String first = generator.generate("https://example.com/path");
        String second = generator.generate("https://example.com/path");

        assertThat(first).hasSize(6);
        assertThat(second).hasSize(6);
        assertThat(second).isNotEqualTo(first);
    }
}
