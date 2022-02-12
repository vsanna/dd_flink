package dev.ishikawa.demo.dd_flink.datasource.configs

import dev.ishikawa.demo.dd_flink.PUserActivityEvent
import dev.ishikawa.demo.dd_flink.PUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaProtobufConsumerConfig {

    @Bean
    open fun userActivityEventProtobufConsumer(): KafkaConsumer<String, PUserActivityEvent> {
        val config = commonConsumerConfig.toMutableMap().also { it[ConsumerConfig.GROUP_ID_CONFIG] =
            DataPublishingService.USER_ACTIVITY_EVENT_PROTOBUF_TOPIC
        }.toMap()

        return KafkaConsumer<String, PUserActivityEvent>(config)
    }

    @Bean
    open fun userProfileEventProtobufConsumer(): KafkaConsumer<String, PUserProfileEvent> {
        val config = commonConsumerConfig.toMutableMap().also { it[ConsumerConfig.GROUP_ID_CONFIG] =
            DataPublishingService.USER_PROFILE_EVENT_PROTOBUF_TOPIC
        }.toMap()
        return KafkaConsumer<String, PUserProfileEvent>(config)
    }

    companion object {
        private val commonConsumerConfig = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaProtobufDeserializer::class.java,
            KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://localhost:8082"
        )
    }
}