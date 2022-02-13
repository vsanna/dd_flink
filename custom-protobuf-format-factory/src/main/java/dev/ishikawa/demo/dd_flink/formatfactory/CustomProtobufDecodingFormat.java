package dev.ishikawa.demo.dd_flink.formatfactory;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.types.RowKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CustomProtobufDecodingFormat accepts interred types of received bytes, and then pass to CustomProtobufDeserializationSchema
 * */
class CustomProtobufDecodingFormat implements DecodingFormat<DeserializationSchema<RowData>> {
    private final ProtobufMessageType protobufMessageType;

    public CustomProtobufDecodingFormat(ProtobufMessageType protobufMessageType) {
        this.protobufMessageType = protobufMessageType;
    }

    @Override
    public DeserializationSchema<RowData> createRuntimeDecoder(DynamicTableSource.Context context, DataType physicalDataType) {
        final TypeInformation<RowData> producedTypeInfo = context.createTypeInformation(physicalDataType);
        final DynamicTableSource.DataStructureConverter converter = context.createDataStructureConverter(physicalDataType);
        final List<LogicalType> parsingTypes = physicalDataType.getLogicalType().getChildren();

        return new CustomProtobufDeserializationSchema(protobufMessageType, parsingTypes, converter, producedTypeInfo);
    }

    @Override
    public ChangelogMode getChangelogMode() {
        return ChangelogMode.newBuilder()
                .addContainedKind(RowKind.INSERT)
                .addContainedKind(RowKind.DELETE)
                .build();
    }

    private static final Logger logger =
            LoggerFactory.getLogger(CustomProtobufDecodingFormat.class);
}
