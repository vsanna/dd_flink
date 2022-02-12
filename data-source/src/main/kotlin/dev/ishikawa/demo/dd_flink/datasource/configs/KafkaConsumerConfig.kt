package dev.ishikawa.demo.dd_flink.datasource.configs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService.Companion.USER_ACTIVITY_EVENT_TOPIC
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService.Companion.USER_PROFILE_EVENT_TOPIC
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaConsumerConfig {

    @Bean
    open fun userActivityEventConsumer(): KafkaConsumer<String, UserActivityEvent> {
        val deserializer = { _: String, bytes: ByteArray -> jsonSerializer.readValue(bytes, UserActivityEvent::class.java) }
        val config = commonConsumerConfig.toMutableMap().also { it[ConsumerConfig.GROUP_ID_CONFIG] = USER_ACTIVITY_EVENT_TOPIC }.toMap()
        return KafkaConsumer<String, UserActivityEvent>(
            config,
            StringDeserializer(),
            deserializer
        )
    }

    @Bean
    open fun userProfileEventConsumer(): KafkaConsumer<String, UserProfileEvent> {
        val deserializer = { _: String, bytes: ByteArray -> jsonSerializer.readValue(bytes, UserProfileEvent::class.java) }
        val config = commonConsumerConfig.toMutableMap().also { it[ConsumerConfig.GROUP_ID_CONFIG] = USER_PROFILE_EVENT_TOPIC }.toMap()

        return KafkaConsumer<String, UserProfileEvent>(
            config,
            StringDeserializer(),
            deserializer
        )
    }

    companion object {
        private val jsonSerializer = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        private val commonConsumerConfig = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            // TODO: ここから. なんかえらってる
            ConsumerConfig.GROUP_ID_CONFIG to "example-group"
        )
    }
}