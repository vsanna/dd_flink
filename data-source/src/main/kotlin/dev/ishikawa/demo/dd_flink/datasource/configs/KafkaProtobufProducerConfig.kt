package dev.ishikawa.demo.dd_flink.datasource.configs

import dev.ishikawa.demo.dd_flink.PUserActivityEvent
import dev.ishikawa.demo.dd_flink.PUserProfileEvent
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaProtobufProducerConfig {

    @Bean
    open fun userActivityEventProtobufProducer(): KafkaProducer<String, PUserActivityEvent> {
        return KafkaProducer<String, PUserActivityEvent>(commonProducerConfig)
    }

    @Bean
    open fun userProfileEventProtobufProducer(): KafkaProducer<String, PUserProfileEvent> {
        return KafkaProducer<String, PUserProfileEvent>(commonProducerConfig)
    }

    companion object {
        private val commonProducerConfig = mapOf(
            ProducerConfig.RETRIES_CONFIG to "10",
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "lz4",
            ProducerConfig.BATCH_SIZE_CONFIG to "16384",
            ProducerConfig.LINGER_MS_CONFIG to "50",
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to "1500",
            ProducerConfig.MAX_BLOCK_MS_CONFIG to "2000",
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaProtobufSerializer::class.java,
            KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://localhost:8082"
        )
    }
}