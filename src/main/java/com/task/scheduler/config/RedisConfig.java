package com.task.scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;

@Configuration
public class RedisConfig {

    @Bean
    LettuceConnectionFactory connectionFactory(@Value("${spring.redis.host:localhost}") String host,
                                                     @Value("${spring.redis.port:6379}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    JedisPool jedisPool(@Value("${spring.redis.host:localhost}") String host,
                      @Value("${spring.redis.port:6379}") int port) {
        return new JedisPool(host, port);
    }
}
