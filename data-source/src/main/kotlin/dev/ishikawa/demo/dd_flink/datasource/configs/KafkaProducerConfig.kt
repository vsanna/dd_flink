package dev.ishikawa.demo.dd_flink.datasource.configs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaProducerConfig {

    @Bean
    open fun userActivityEventProducer(): KafkaProducer<String, UserActivityEvent> {
        val serializer = { _: String, data: UserActivityEvent -> jsonSerializer.writeValueAsBytes(data) }
        return KafkaProducer<String, UserActivityEvent>(
            commonProducerConfig,
            StringSerializer(),
            serializer
        )
    }

    @Bean
    open fun userProfileEventProducer(): KafkaProducer<String, UserProfileEvent> {
        val serializer = { _: String, data: UserProfileEvent -> jsonSerializer.writeValueAsBytes(data) }
        return KafkaProducer<String, UserProfileEvent>(
            commonProducerConfig,
            StringSerializer(),
            serializer
        )
    }

    companion object {
        private val jsonSerializer = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        private val commonProducerConfig = mapOf(
            ProducerConfig.RETRIES_CONFIG to "10",
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "lz4",
            ProducerConfig.BATCH_SIZE_CONFIG to "16384",
            ProducerConfig.LINGER_MS_CONFIG to "50",
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to "1500",
            ProducerConfig.MAX_BLOCK_MS_CONFIG to "2000",
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092"
        )
    }
}

data class UserActivityEvent(
    val eventId: String = "",
    val userId: String = "",
    val action: UserAction = UserAction(),
    val ts: Long = 0
)

data class UserAction(
    val actionType: UserActionType = UserActionType.CLICK_URL,
    val data: String = "{}" // stringified json
)

enum class UserActionType {
    CLICK_URL,
    PAGE_BACK,
    PAGE_FORWARD,
    SCROLL_TO_BOTTOM,
    SCROLL_TO_TOP
}


data class UserProfileEvent(
    val eventId: String = "",
    val userId: String = "",
    val type: UserProfileEventType = UserProfileEventType.UPDATE,
    val data: String = "{}", // stringified json
    val ts: Long = 0
)

enum class UserProfileEventType {
    CREATION,
    UPDATE,
    DELETION
}