package dev.ishikawa.demo.dd_flink.datasource.service

import dev.ishikawa.demo.dd_flink.PUserActivityEvent
import dev.ishikawa.demo.dd_flink.PUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserActivityProtobufEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserProfileProtobufEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DataPublishingService(
    private val userProfileEventProducer: KafkaProducer<String, UserProfileEvent>,
    private val userActivityEventProducer: KafkaProducer<String, UserActivityEvent>,
    private val userProfileEventProtobufProducer: KafkaProducer<String, PUserProfileEvent>,
    private val userActivityEventProtobufProducer: KafkaProducer<String, PUserActivityEvent>
) {
    suspend fun execute() = scope.async {
        // json
        launch { loopUserProfileEvent() }
        launch { loopUserActivityEvent() }

        // protobuf
        launch { loopUserProfileProtobufEvent() }
        launch { loopUserActivityProtobufEvent() }
    }

    private suspend fun loopUserProfileEvent()  {
        log.info("====starting: loopUserProfileEvent")
        while (true) {
            val event = genUserProfileEvent()
            userProfileEventProducer.send(
                ProducerRecord(
                    USER_PROFILE_EVENT_TOPIC,
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending UserProfileEvent. message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent UserProfileEvent eventId: ${event.eventId}")
                }
            }

            delay(PROFILE_INTERVAL_MSEC)
        }
    }

    private suspend fun loopUserActivityEvent() {
        log.info("====starting: loopUserActivityEvent")
        while (true) {
            val event = genUserActivityEvent()
            userActivityEventProducer.send(
                ProducerRecord(
                    USER_ACTIVITY_EVENT_TOPIC,
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending UserActivityEvent. message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent UserActivityEvent eventId : ${event.eventId}")
                }
            }
            delay(ACTIVITY_INTERVAL_MSEC)
        }
    }


    private suspend fun loopUserProfileProtobufEvent() {
        log.info("====starting: loopUserProfileProtobufEvent")
        while (true) {
            val event = genUserProfileProtobufEvent()
            userProfileEventProtobufProducer.send(
                ProducerRecord(
                    USER_PROFILE_EVENT_PROTOBUF_TOPIC,
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending PUserProfileEvent. message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent PUserProfileEvent eventId: ${event.eventId}")
                }
            }

            delay(PROFILE_INTERVAL_MSEC)
        }
    }

    private suspend fun loopUserActivityProtobufEvent() {
        log.info("====starting: loopUserActivityProtobufEvent")
        while (true) {
            val event = genUserActivityProtobufEvent()
            userActivityEventProtobufProducer.send(
                ProducerRecord(
                    USER_ACTIVITY_EVENT_PROTOBUF_TOPIC,
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending PUserActivityEvent. message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent PUserActivityEvent eventId : ${event.eventId}")
                }
            }
            delay(ACTIVITY_INTERVAL_MSEC)
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)

        private val log = LoggerFactory.getLogger(DataPublishingService::class.java)

        const val USER_PROFILE_EVENT_TOPIC = "user-profile-event-topic"
        const val USER_ACTIVITY_EVENT_TOPIC = "user-activity-event-topic"
        const val USER_PROFILE_EVENT_PROTOBUF_TOPIC = "user-profile-event-protobuf-topic"
        const val USER_ACTIVITY_EVENT_PROTOBUF_TOPIC = "user-activity-event-protobuf-topic"

        private const val ACTIVITY_INTERVAL_MSEC = 5000L
        private const val PROFILE_INTERVAL_MSEC = 10000L
    }
}