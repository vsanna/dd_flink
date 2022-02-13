data-source is a simple application that generates dummy data into kafka.
It has 2 event types, and publishes the two event types as json or protobuf, as such it uses 4 topics.
```kotlin
# SEE: DataPublishingService.
const val USER_PROFILE_EVENT_TOPIC = "user-profile-event-topic"
const val USER_ACTIVITY_EVENT_TOPIC = "user-activity-event-topic"
const val USER_PROFILE_EVENT_PROTOBUF_TOPIC = "user-profile-event-protobuf-topic"
const val USER_ACTIVITY_EVENT_PROTOBUF_TOPIC = "user-activity-event-protobuf-topic"
```

# how to start
```shell
docker-compose up -d
{start via IntelliJ or gradle}
```
