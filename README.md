# TaskScheduler

A lightweight Java Spring Boot task scheduling service that uses Redis Streams for task queuing and MySQL for persistence. The project is containerized with Docker Compose for easy local development.

## Features

- Produces and consumes tasks via Redis Streams
- Consumer group support for scalable workers
- MySQL data storage
- Docker Compose setup for app, MySQL and Redis

## Key components

- src/main/java/com/task/scheduler/config/RedisConfig.java — Lettuce connection factory and RedisTemplate
- src/main/java/com/task/scheduler/config/RedisStreamInitializer.java — stream and consumer-group initializer
- src/main/java/com/task/scheduler/core/service/TaskProducerService.java — pushes tasks to the Redis stream
- src/main/java/com/task/scheduler/core/service/TaskStreamConsumer.java — stream consumer that processes tasks
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
   - spring.redis.host
   - spring.redis.port
   - spring.datasource.url
   - spring.datasource.username
   - spring.datasource.password

2. Build and run:

   ./mvnw clean package
   java -jar target/*.jar

## Redis Streams and consumer groups

- Stream name: `default`
- Consumer group: `worker-group`

The code attempts to create the consumer group on startup (see RedisStreamInitializer), but the consumer may start earlier than the initializer in some environments. If you see errors like:

  NOGROUP No such key 'default' or consumer group 'worker-group' in XREADGROUP

Either create the group manually in Redis or ensure the application creates it before subscribing.

Manual creation command (run inside the redis container):

  docker compose exec redis redis-cli XGROUP CREATE default worker-group $ MKSTREAM

Or from any redis-cli pointing at the redis service:

  redis-cli -h redis XGROUP CREATE default worker-group $ MKSTREAM

Recommended fix in code: ensure the consumer creates the group right before subscribing (see TaskStreamConsumer.startConsumer()).

## Troubleshooting Redis connectivity

1. Confirm environment variables inside the app container:

   docker compose exec app printenv SPRING_REDIS_HOST SPRING_REDIS_PORT

2. Confirm the app uses those values (RedisConfig reads `spring.redis.host` and `spring.redis.port`).

3. Test connectivity from the app container:

   docker compose exec app ping redis
   docker compose exec app redis-cli -h redis ping

4. Check service health and ports:

   docker compose ps
   docker compose logs redis

## Useful Maven dependencies

- spring-boot-starter-data-redis-reactive
- spring-data-redis (Lettuce)

## Contributing

Contributions and improvements welcome. Open issues or PRs with clear descriptions.

## License

MIT (or choose your preferred license)
