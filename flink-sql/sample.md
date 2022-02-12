## prerequirement
- Make sure that you have assigned a lot of mem for Docker(or Docker Desktop)
  - 8+GB is recommended

## Start and exec into flink container
```shell
docker-compose up -d
docker exec -it dd_flink_taskmanager1_1 /bin/bash
```


## Download flink-connector-kafka
```shell
curl -L https://repo1.maven.org/maven2/org/apache/flink/flink-connector-kafka_2.12/1.14.3/flink-connector-kafka_2.12-1.14.3.jar -o flink-connector-kafka.jar
curl -L https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.1.0/kafka-clients-3.1.0.jar -o kafka-clients.jar
```


## start Flink SQL
```shell
./bin/sql-client.sh --jar flink-connector-kafka.jar --jar kafka-clients.jar
```

## Consume json events via  Flink SQL
```sql
SET 'sql-client.execution.result-mode' = 'tableau';
SET 'sql-client.verbose' = 'true';


# data class UserProfileEvent(
#     val eventId: String,
#     val userId: String,
#     val type: UserProfileEventType,
#     val data: String, // stringified json
#     val timestamp: Long
# )
SHOW TABLES;
CREATE TABLE UserActivityEvent (
    `eventId` STRING,
    `userId` STRING,
    `action` STRING,
    `ts` BIGINT,
    -- available metadata
    `partition` BIGINT METADATA VIRTUAL,
    `ts_meta` TIMESTAMP(3) METADATA FROM 'timestamp',
    `offset` BIGINT METADATA VIRTUAL  -- from Kafka connector
) WITH (
    'connector' = 'kafka',
    'topic' = 'user-activity-event-topic',
    'properties.bootstrap.servers' = 'kafka_broker:9092',
    -- 'properties.group.id' = 'testGroup',
    'scan.startup.mode' = 'earliest-offset',
    'value.format' = 'json'
);

SHOW TABLES;

SELECT * FROM UserActivityEvent LIMIT 3;

CREATE TABLE UserProfileEvent(
    `eventId` STRING,
    `userId` STRING,
    `type` STRING,
    `data` STRING,
    -- available metadata
    `partition` BIGINT METADATA VIRTUAL,
    `ts_meta` TIMESTAMP(3) METADATA FROM 'timestamp',
    `offset` BIGINT METADATA VIRTUAL  -- from Kafka connector
) WITH (
    'connector' = 'kafka',
    'topic' = 'user-profile-event-topic',
    'properties.bootstrap.servers' = 'kafka_broker:9092',
    -- 'properties.group.id' = 'testGroup',
    'scan.startup.mode' = 'earliest-offset',
    'value.format' = 'json'
);

SELECT userId, type, COUNT(*) FROM UserProfileEvent GROUP BY userId, type;

```


## Consume protobuf events via  Flink SQL
```sql
SET 'sql-client.execution.result-mode' = 'tableau';
SET 'sql-client.verbose' = 'true';


# data class UserProfileEvent(
#     val eventId: String,
#     val userId: String,
#     val type: UserProfileEventType,
#     val data: String, // stringified json
#     val timestamp: Long
# )
SHOW TABLES;
CREATE TABLE PUserActivityEvent (
    `eventId` STRING,
    `userId` STRING,
    `action` STRING,
    `ts` BIGINT
) WITH (
    'connector' = 'protobuf',
    'topic' = 'user-activity-event-protobuf-topic',
    'properties.bootstrap.servers' = 'kafka_broker:9092',
    -- 'properties.group.id' = 'testGroup',
    -- 'scan.startup.mode' = 'earliest-offset',
    -- 'value.format' = 'json'
);

SHOW TABLES;

SELECT * FROM PUserActivityEvent LIMIT 3;
```


# Appendix
## install kafka cli for debugging
```shell
curl -L https://dlcdn.apache.org/kafka/3.1.0/kafka_2.13-3.1.0.tgz -o kafka.tgz
tar xvf kafka.tgz
# NOTE: the path directory depends on your env
./kafka_2.13-3.1.0/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic user-activity-event-topic


apt install iproute2 -y
apt install dnsutils -y 
```

# Appendix
## install kafka cli for debugging
```shell
curl -L https://dlcdn.apache.org/kafka/3.1.0/kafka_2.13-3.1.0.tgz -o kafka.tgz
tar xvf kafka.tgz
# NOTE: the path directory depends on your env
./kafka_2.13-3.1.0/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic user-activity-event-topic


apt install iproute2 -y
apt install dnsutils -y 
```

