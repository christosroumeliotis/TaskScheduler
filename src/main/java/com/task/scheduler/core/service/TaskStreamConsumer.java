package com.task.scheduler.core.service;

import com.task.scheduler.config.RedisStreamInitializer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class TaskStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate redisTemplate;
    private static final Logger log = LoggerFactory.getLogger(TaskStreamConsumer.class);
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final String workerId = "worker-" + UUID.randomUUID().toString().substring(0, 8);
    private Subscription subscription;
    private final TaskExecutionService executionService;

    public TaskStreamConsumer(StringRedisTemplate redisTemplate, TaskExecutionService executionService) {
        this.redisTemplate = redisTemplate;
        this.executionService = executionService;
    }

    @PostConstruct
    public void startConsumer() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String,
                MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build();

        container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

        subscription = container.receive(
                Consumer.from(RedisStreamInitializer.CONSUMER_GROUP, workerId),
                StreamOffset.create(RedisStreamInitializer.QUEUE_NAME, ReadOffset.lastConsumed()),
                this
        );
        container.start();
        log.info("Started Redis Worker Listener [{}] for stream [{}]", workerId, RedisStreamInitializer.QUEUE_NAME);
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            executionService.processTaskExecution(record.getValue(), workerId);

            redisTemplate.opsForStream().acknowledge(
                    RedisStreamInitializer.QUEUE_NAME,
                    RedisStreamInitializer.CONSUMER_GROUP,
                    record.getId()
            );
        } catch (Exception e) {
            log.error("Fatal exception processing Redis stream record {}", record.getId(), e);
        }
    }

    @PreDestroy
    public void stopConsumer() {
        if (subscription != null)
            subscription.cancel();
        if (container != null)
            container.stop();
    }
}
