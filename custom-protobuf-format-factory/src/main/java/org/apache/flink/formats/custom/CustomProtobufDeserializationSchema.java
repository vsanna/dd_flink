package org.apache.flink.formats.custom;

import com.google.protobuf.util.JsonFormat;
import dev.ishikawa.demo.dd_flink.PUserActivityEvent;
import dev.ishikawa.demo.dd_flink.PUserProfileEvent;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.table.connector.RuntimeConverter;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * CustomProtobufDeserializationSchema has actual deserialization logic.
 * */
class CustomProtobufDeserializationSchema implements DeserializationSchema<RowData> {
    private final ProtobufMessageType protobufMessageType;
    private final List<LogicalType> parsingTypes;
    private final DynamicTableSource.DataStructureConverter converter;
    private final TypeInformation<RowData> producedTypeInfo;

    public CustomProtobufDeserializationSchema(
            ProtobufMessageType protobufMessageType,
            List<LogicalType> parsingTypes,
            DynamicTableSource.DataStructureConverter converter,
            TypeInformation<RowData> producedTypeInfo
    ) {
        this.protobufMessageType = protobufMessageType;
        this.parsingTypes = parsingTypes;
        this.converter = converter;
        this.producedTypeInfo = producedTypeInfo;

        logger.info("initializing CustomProtobufDeserializationSchema");
        logger.info("producedTypeInfo: {}", producedTypeInfo);
        logger.info("parsingTypes: {}", parsingTypes);
        logger.info("eventClassType: {}", protobufMessageType);
    }

    @Override
    public TypeInformation<RowData> getProducedType() {
        return producedTypeInfo;
    }

    @Override
    public void open(InitializationContext context) {
        converter.open(RuntimeConverter.Context.create(CustomProtobufDeserializationSchema.class.getClassLoader()));
    }

    /**
     * deserialize does deserialization according to message type.
     * */
    @Override
    public RowData deserialize(byte[] bytes) {
        final Row row = new Row(RowKind.INSERT, parsingTypes.size());
        logger.info("bytes.length: {}", bytes.length);
        logger.info("bytes: {}", bytes);

        // drop kafka header
        byte[] slicedBytes = KafkaHeaderRemover.bytesWithoutKafkaHeader(bytes, protobufMessageType);

        try {
            if(protobufMessageType == ProtobufMessageType.USER_ACTIVITY) {
                // 1. parse bytes to PUserActivityEvent
                // 2. build row object by populating values of PUserActivityEvent
                PUserActivityEvent event = PUserActivityEvent.parseFrom(slicedBytes);
                logger.info("event: {}", event);

                row.setField(0, event.getEventId());
                row.setField(1, event.getUserId());

                // NOTE: it's still under research how to pass nested user-defined type
                String stringifiedAction = JsonFormat.printer().print(event.getAction()).replaceAll("\n", "");
                logger.info("stringifiedAction: {}", stringifiedAction);
                row.setField(2, stringifiedAction);

                row.setField(3, event.getTs());
            } else if (protobufMessageType == ProtobufMessageType.USER_PROFILE) {
                // 1. parse bytes to PUserProfileEvent
                // 2. build row object by populating values of PUserProfileEvent
                PUserProfileEvent event = PUserProfileEvent.parseFrom(slicedBytes);
                logger.info("event: {}", event);

                row.setField(0, event.getEventId());
                row.setField(1, event.getUserId());
                row.setField(2, event.getType().toString());
                row.setField(3, event.getData());
                row.setField(4, event.getTs());
            }
            return (RowData) converter.toInternal(row);
        } catch (Exception e) {
            logger.error("failed deserializing event {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(RowData event) {
        return false;
    }

    private static final Logger logger =
            LoggerFactory.getLogger(CustomProtobufDeserializationSchema.class);

    /**
     * KafkaHeaderRemover removes some heading bytes according to each message type.
     * The length of bytes to remove depends on the message's position in .proto file.
     * bytes to remove = magic-number(1byte) + schema-id(4bytes) + message-indexes(0~byte)
     *
     * SEE: https://stackoverflow.com/questions/70568413/deserialize-protobuf-kafka-messages-with-flink/70571290
     * SEE: https://docs.confluent.io/platform/current/schema-registry/serdes-develop/index.html#wire-format
     * */
    static class KafkaHeaderRemover {
        static private byte[] bytesWithoutKafkaHeader(byte[] originalBytes, ProtobufMessageType protobufMessageType) {
            int positionOfMessageInProtofile = protobufMessageType.positionInProtoFile;
            int kafkaHeaderLength = calcLengthOfKafkaHeader(positionOfMessageInProtofile);
            if(originalBytes.length <= kafkaHeaderLength) {
                logger.error("passed bytes's length is less than or equal to kafka's header, which means it doesn't have protobuf message. it's unexpected");
                return null;
            }
            return Arrays.copyOfRange(originalBytes, kafkaHeaderLength + 1, originalBytes.length);
        }

        static private int calcLengthOfKafkaHeader(int positionOfMessageInProtofile) {
            return KAFKA_HEADER_BYTE_LENGTH + calcLengthOfMessageIndeces(positionOfMessageInProtofile);
        }

        /**
         * Currently, assuming that passed message type is always located in top-level.
         * So, length of message-index is always same as its position
         *
         * This method is placeholder for the future when non top-level message types come here
         * */
        static private int calcLengthOfMessageIndeces(int positionOfMessageInProtofile) {
            return positionOfMessageInProtofile;
        }

        private static final int KAFKA_HEADER_BYTE_LENGTH = 5;
    }
}
