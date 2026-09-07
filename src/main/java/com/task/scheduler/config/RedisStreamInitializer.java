//package com.task.scheduler.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.stream.ReadOffset;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//@Configuration
//public class RedisStreamInitializer {
//
//    private static final Logger log = LoggerFactory.getLogger(RedisStreamInitializer.class);
//    public static final String QUEUE_NAME = "default";
//    public static final String CONSUMER_GROUP = "worker-group";
//
//    @Bean
//    public CommandLineRunner initRedisStream(StringRedisTemplate redisTemplate) {
//        return args -> {
//            try {
//                //If group doesn't exist, create a new one
//                redisTemplate.opsForStream().createGroup(QUEUE_NAME, ReadOffset.from("0"), CONSUMER_GROUP);
//                log.info("Created Consumer Group '{}' on Stream '{}'", CONSUMER_GROUP, QUEUE_NAME);
//            } catch (Exception e) {
//                log.info("Consumer Group '{}' already active on Stream '{}'", CONSUMER_GROUP, QUEUE_NAME);
//            }
//        };
//    }
//}
