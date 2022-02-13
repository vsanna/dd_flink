custom-protobuf-format-factory is supposed to be used as a jar file for FlinkSQL.

For each `CREATE TABLE` statements whose source is encoded by protobuf need their own FormatFactory.

```shell
# in local
gradle shadowJar

# somewhere you can use FlinkSQL
./bin/sql-client.sh \
  --jar custom-protobuf-format-factory-1.0-SNAPSHOT-all.jar \
  --jar flink-connector-kafka.jar \
  --jar kafka-clients.jar \
  --jar kafka-protobuf-serializer.jar
```