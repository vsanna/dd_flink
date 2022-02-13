package dev.ishikawa.demo.dd_flink.formatfactory;

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
    private final DynamicTableSource.DataStructureConverter converter;

    /**
     * producedTypeInfo is fields of `CREATE TABLE` query. Not fields of message type.
     * */
    private final TypeInformation<RowData> producedTypeInfo;
    /**
     * parsingTypes is list of types of the fields in `CREATE TABLE`
     * */
    private final List<LogicalType> parsingTypes;

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

                /*
                * mapping `CREATE TABLE` statement into row.
                * CREATE TABLE PUserActivityEvent (
                *   `eventId` STRING,
                *   `userId` STRING,
                *   `action` ROW<actionType STRING, data STRING>,
                *   `ts` BIGINT
                * )
                * */
                row.setField(0, event.getEventId());
                row.setField(1, event.getUserId());

                // for the case when it's enough just to translate inner object as stringified json.
                // row.setField(2, JsonFormat.printer().print(event.getAction()).replaceAll("\n", ""));

                final Row innerRow = new Row(RowKind.INSERT, 2); // 2 == length of fields in PUserActivity
                innerRow.setField(0, event.getAction().getActionType().toString());
                innerRow.setField(1, event.getAction().getData());
                row.setField(2, innerRow);

                row.setField(3, event.getTs());
            } else if (protobufMessageType == ProtobufMessageType.USER_PROFILE) {
                PUserProfileEvent event = PUserProfileEvent.parseFrom(slicedBytes);

                /*
                 * mapping `CREATE TABLE` statement into row.
                 * CREATE TABLE PUserProfileEvent (
                 *   `eventId` STRING,
                 *   `userId` STRING,
                 *   `type` STRING,
                 *   `data` STRING,
                 *   `ts` BIGINT
                 * )
                 * */
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
            return Arrays.copyOfRange(originalBytes, kafkaHeaderLength, originalBytes.length);
        }

        static private int calcLengthOfKafkaHeader(int positionOfMessageInProtofile) {
            return KAFKA_HEADER_BYTE_LENGTH + calcLengthOfMessageIndexes(positionOfMessageInProtofile);
        }

        /**
         * Currently, assuming that passed message type is always located in top-level.
         *
         * */
        static private int calcLengthOfMessageIndexes(int positionOfMessageInProtofile) {
            if(positionOfMessageInProtofile == 0) {
                return 1;
            } else {
                return 2;
            }
        }

        private static final int KAFKA_HEADER_BYTE_LENGTH = 5;
    }
}
