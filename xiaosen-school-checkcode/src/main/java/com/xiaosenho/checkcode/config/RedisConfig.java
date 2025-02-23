package com.xiaosenho.checkcode.config;

/**
 * @author: 作者
 * @create: 2025-02-23 16:21
 * @Description:
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置键的序列化器为 StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());

        // 设置值的序列化器为 Jackson2JsonRedisSerializer
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        // 设置 Hash 类型的键和值的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return template;
    }
}