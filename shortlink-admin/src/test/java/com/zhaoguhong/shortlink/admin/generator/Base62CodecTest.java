package com.zhaoguhong.shortlink.admin.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base62CodecTest {

    @Test
    void shouldEncodeToExpectedBase62Value() {
        assertThat(Base62Codec.encode(0)).isEqualTo("0");
        assertThat(Base62Codec.encode(61)).isEqualTo("z");
        assertThat(Base62Codec.encode(62)).isEqualTo("10");
        assertThat(Base62Codec.encode(3843)).isEqualTo("zz");
    }

    @Test
    void shouldThrowWhenNegativeNumberProvided() {
        assertThatThrownBy(() -> Base62Codec.encode(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
