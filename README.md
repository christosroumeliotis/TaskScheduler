# TaskScheduler

A lightweight Java Spring Boot task scheduling service that uses Redis Streams for task queuing and MySQL for persistence. The project is containerized with Docker Compose for easy local development.

## Features

- Produces and consumes tasks via Redis Streams
- Consumer group support for scalable workers
- MySQL data storage
- Docker Compose setup for app, MySQL and Redis
- Rate limiter using a Redis-backed sliding window counter (per-key request limiting)

## Key components

- src/main/java/com/task/scheduler/config/RedisConfig.java — Lettuce connection factory and RedisTemplate
- src/main/java/com/task/scheduler/config/RedisStreamInitializer.java — stream and consumer-group initializer
- src/main/java/com/task/scheduler/core/service/TaskProducerService.java — pushes tasks to the Redis stream
- src/main/java/com/task/scheduler/core/service/TaskStreamConsumer.java — stream consumer that processes tasks
- src/main/java/com/task/scheduler/filter/SlidingWindowCounterLimiter.java — Servlet filter implementing a Redis-backed sliding-window rate limiter
- docker-compose.yml — app, db (MySQL) and redis services

## Requirements

- Java 17+ (or compatible JDK used by project)
- Maven
- Docker & Docker Compose (for containerized run)

## Quick start (Docker)

1. Create a `.env` in the project root (the repo contains a sample `.env`) with values for:
   - MYSQL_ROOT_PASSWORD
   - MYSQL_DATABASE
   - SPRING_REDIS_HOST (default `redis` in docker-compose)
   - SPRING_REDIS_PORT (default `6379`)

2. Rebuild and run:

   docker compose build --no-cache
   docker compose up -d --force-recreate --build

3. View logs:

   docker compose logs -f app
   docker compose logs -f redis

## Run locally (without Docker)

1. Configure `src/main/resources/application.properties` or provide environment variables matching Spring properties:
   - spring.datasource.url
   - spring.datasource.username
   - spring.datasource.password

2. Build and run:

   ./mvnw clean package
   java -jar target/*.jar

## Redis Streams and consumer groups

- Stream name: `default`
- Consumer group: `worker-group`

## Rate limiter (Sliding window with Redis)

A Redis-backed sliding-window rate limiter has been added to support per-key request limiting. It uses a time-windowed counter stored in Redis to make rate checks accurate and efficient across distributed instances.

Key details

- Implementation: src/main/java/com/task/scheduler/filter/SlidingWindowCounterLimiter.java — a Servlet Filter that enforces limits per key (e.g., IP or API key).
- Algorithm: sliding-window counter using Redis sorted sets or timestamps to count requests within a rolling window.
- Estimated Count = (Previous Window Requests x (1 - current window elapsed time)) + Current Window Requests
