package com.zhaoguhong.shortlink.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaoguhong.shortlink.common.entity.ShortLink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 序列化配置。
 *
 * @author zhaoguhong
 * @date 2026/2/28
 */
@Configuration
public class RedisConfig {

    /**
     * 短链对象专用 RedisTemplate，统一在框架层处理 JSON 序列化/反序列化。
     */
    @Bean
    public RedisTemplate<String, ShortLink> shortLinkRedisTemplate(RedisConnectionFactory connectionFactory,
                                                                   ObjectMapper objectMapper) {
        RedisTemplate<String, ShortLink> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        return template;
    }
}
