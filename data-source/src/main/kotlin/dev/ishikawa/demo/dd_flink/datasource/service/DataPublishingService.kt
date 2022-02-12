package dev.ishikawa.demo.dd_flink.datasource.service

import dev.ishikawa.demo.dd_flink.PUserActivityEvent
import dev.ishikawa.demo.dd_flink.PUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserActivityProtobufEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserProfileProtobufEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
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
    fun execute() = runBlocking {
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
                    "user-profile-event-topic",
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent UserProfileEvent event: $event")
                }
            }

            delay(5000)
        }
    }

    private suspend fun loopUserActivityEvent() {
        log.info("====starting: loopUserActivityEvent")
        while (true) {
            val event = genUserActivityEvent()
            userActivityEventProducer.send(
                ProducerRecord(
                    "user-activity-event-topic",
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent UserActivityEvent event : $event")
                }
            }
            delay(1000)
        }
    }


    private suspend fun loopUserProfileProtobufEvent() {
        log.info("====starting: loopUserProfileProtobufEvent")
        while (true) {
            val event = genUserProfileProtobufEvent()
            userProfileEventProtobufProducer.send(
                ProducerRecord(
                    "user-profile-event-protobuf-topic",
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent PUserProfileEvent event: $event")
                }
            }

            delay(5000)
        }
    }

    private suspend fun loopUserActivityProtobufEvent() {
        log.info("====starting: loopUserActivityProtobufEvent")
        while (true) {
            val event = genUserActivityProtobufEvent()
            userActivityEventProtobufProducer.send(
                ProducerRecord(
                    "user-activity-event-protobuf-topic",
                    event.eventId,
                    event
                )
            ) { metadata, exception ->
                if (exception != null) {
                    log.error("failed in sending message: metadata =  $metadata, event = $event", exception)
                } else {
                    log.info("sent PUserActivityEvent event : $event")
                }
            }
            delay(1000)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DataPublishingService::class.java)
    }
}