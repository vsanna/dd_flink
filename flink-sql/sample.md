## prerequisite
- Make sure that you have assigned a lot of mem for Docker(or Docker Desktop)
  - it's confirmed 8+GB meme is enough

## Start and upload/download necessary jar files 
To run FlinkSQL with protobuf, it needs some jar files
1. flink-connector-kafka_2.12-1.14.3.jar
2. kafka-clients-3.1.0.jar 
3. kafka-protobuf-serializer-5.5.1.jar
4. custom-protobuf-format-factory's jar file
5. my own protobuf's class files

For 4 and 5, we need to build and cp to docker container.
For 1,2 and 3, we can use curl to download them in docker container.

### start docker containers
```shell
(cd /path/to/dd_flink)
docker-compose up -d
```

### build and make jar files, and then cp them to docker container
```shell
# these scripts are util shellscripts. you don't need to use them, and you can do as you like
./custom-protobuf-format-factory/uploadToDocker.sh
./proto/publishToLocal.sh
./proto/uploadToDocker.sh

docker exec -it dd_flink_taskmanager1_1 /bin/bash
```


### Download flink-connector-kafka
```shell
docker exec -it dd_flink_taskmanager1_1 /bin/bash

(in a docker container)
curl -L https://repo1.maven.org/maven2/org/apache/flink/flink-connector-kafka_2.12/1.14.3/flink-connector-kafka_2.12-1.14.3.jar -o flink-connector-kafka.jar
curl -L https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.1.0/kafka-clients-3.1.0.jar -o kafka-clients.jar
curl -L https://packages.confluent.io/maven/io/confluent/kafka-protobuf-serializer/5.5.1/kafka-protobuf-serializer-5.5.1.jar -o kafka-protobuf-serializer.jar
```


## start Flink SQL
```shell
./bin/sql-client.sh \
--jar flink-connector-kafka.jar \
--jar kafka-clients.jar \
--jar kafka-protobuf-serializer.jar \
--jar demo-0.1.0.jar \
--jar custom-protobuf-format-factory-1.0-SNAPSHOT-all.jar

```

## Consume json events via  Flink SQL
```sql
SET 'sql-client.execution.result-mode' = 'tableau';
SET 'sql-client.verbose' = 'true';

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
    'properties.bootstrap.servers' = 'kafka_broker:29092',
    'properties.group.id' = 'flink-sql-user-activity',
    'value.format' = 'json',
    'scan.startup.mode' = 'earliest-offset'
);

SELECT COUNT(*) FROM UserActivityEvent;


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
    'properties.bootstrap.servers' = 'kafka_broker:29092',
    'scan.startup.mode' = 'earliest-offset',
    'value.format' = 'json'
);

SELECT userId, type, COUNT(*) FROM UserProfileEvent GROUP BY userId, type;
```


## Consume protobuf events via  Flink SQL
```sql
SET 'sql-client.execution.result-mode' = 'tableau';
SET 'sql-client.verbose' = 'true';

CREATE TABLE PUserActivityEvent (
    `eventId` STRING,
    `userId` STRING,
    `action` STRING,
    `ts` BIGINT
) WITH (
    'connector' = 'kafka',
    'topic' = 'user-activity-event-protobuf-topic',
    'properties.bootstrap.servers' = 'kafka_broker:29092',
    'properties.group.id' = 'flink-sql-user-activity-protobuf',
    'value.format' = 'custom-protobuf',
    'scan.startup.mode' = 'earliest-offset',
    'properties.auto.offset.reset' = 'earliest',
    'value.custom-protobuf.protobuf.eventclass' = 'USER_ACTIVITY_EVENT'
);

SELECT COUNT(*) FROM PUserActivityEvent;
SELECT * FROM PUserActivityEvent;
SELECT userId, COUNT(*) AS cnt FROM PUserActivityEvent GROUP BY userId;








CREATE TABLE PUserProfileEvent (
  `eventId` STRING,
  `userId` STRING,
  `type` STRING,
  `data` STRING
) WITH (
  'connector' = 'kafka',
  'topic' = 'user-profile-event-protobuf-topic',
  'properties.bootstrap.servers' = 'kafka_broker:29092',
  'properties.group.id' = 'flink-sql-user-activity-protobuf',
  'value.format' = 'custom-protobuf',
  'scan.startup.mode' = 'earliest-offset',
  'properties.auto.offset.reset' = 'earliest',
  'value.custom-protobuf.protobuf.eventclass' = 'USER_PROFILE_EVENT'
);

SELECT COUNT(*) AS count FROM PUserProfileEvent;
SELECT * FROM PUserProfileEvent;
SELECT userId, type, COUNT(*) AS cnt FROM PUserProfileEvent GROUP BY userId, type;


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

