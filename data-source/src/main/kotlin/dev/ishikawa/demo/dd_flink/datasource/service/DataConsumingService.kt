package dev.ishikawa.demo.dd_flink.datasource.service

import dev.ishikawa.demo.dd_flink.PUserActivityEvent
import dev.ishikawa.demo.dd_flink.PUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserActivityProtobufEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataGenerator.genUserProfileProtobufEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService.Companion.USER_ACTIVITY_EVENT_PROTOBUF_TOPIC
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService.Companion.USER_ACTIVITY_EVENT_TOPIC
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService.Companion.USER_PROFILE_EVENT_PROTOBUF_TOPIC
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService.Companion.USER_PROFILE_EVENT_TOPIC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class DataConsumingService(
    private val userProfileEventConsumer: KafkaConsumer<String, UserProfileEvent>,
    private val userActivityEventConsumer: KafkaConsumer<String, UserActivityEvent>,
    private val userProfileEventProtobufConsmer: KafkaConsumer<String, PUserProfileEvent>,
    private val userActivityEventProtobufConsumer: KafkaConsumer<String, PUserActivityEvent>
) {
    suspend fun execute() = scope.async {
        // json
        launch { loopUserProfileEvent() }
        launch { loopUserActivityEvent() }

        // protobuf
        launch { loopUserProfileProtobufEvent() }
        launch { loopUserActivityProtobufEvent() }
    }

    private fun loopUserProfileEvent()  {
        log.info("====starting: loopUserProfileEvent")
        userProfileEventConsumer.subscribe(listOf(USER_PROFILE_EVENT_TOPIC))
        while (true) {
            val records = userProfileEventConsumer.poll(PROFILE_INTERVAL_MSEC)
            records.forEach { consumerRecord ->
                log.info("consumed UserProfileEvent eventId: ${consumerRecord.value().eventId}")
            }
        }
    }

    private fun loopUserActivityEvent() {
        log.info("====starting: loopUserActivityEvent")
        userActivityEventConsumer.subscribe(listOf(USER_ACTIVITY_EVENT_TOPIC))
        while (true) {
            val records = userActivityEventConsumer.poll(ACTIVITY_INTERVAL_MSEC)
            records.forEach { consumerRecord ->
                log.info("consumed UserActivityEvent eventId: ${consumerRecord.value().eventId}")
            }
        }
    }


    private fun loopUserProfileProtobufEvent() {
        log.info("====starting: loopUserProfileProtobufEvent")
        userProfileEventProtobufConsmer.subscribe(listOf(USER_PROFILE_EVENT_PROTOBUF_TOPIC))
        while (true) {
            val records = userProfileEventProtobufConsmer.poll(PROFILE_INTERVAL_MSEC)
            records.forEach { consumerRecord ->
                log.info("consumed PUserProfileEvent eventId: ${consumerRecord.value().eventId}")
            }
        }
    }

    private fun loopUserActivityProtobufEvent() {
        log.info("====starting: loopUserActivityProtobufEvent")
        userActivityEventProtobufConsumer.subscribe(listOf(USER_ACTIVITY_EVENT_PROTOBUF_TOPIC))
        while (true) {
            val records = userActivityEventProtobufConsumer.poll(ACTIVITY_INTERVAL_MSEC)
            records.forEach { consumerRecord ->
                log.info("consumed PUserActivityEvent eventId: ${consumerRecord.value().eventId}")
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        private val log = LoggerFactory.getLogger(DataConsumingService::class.java)
        private val ACTIVITY_INTERVAL_MSEC = Duration.ofMillis(2000L)
        private val PROFILE_INTERVAL_MSEC = Duration.ofMillis(5000L)
    }
}