package org.apache.flink.formats.custom;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * CustomProtobufFormatFactory defines and translates options for this format, and then create CustomProtobufDecodingFormat
 * Actual deserialization is handled in CustomProtobufDeserializationSchema
 * @see org.apache.flink.formats.custom.CustomProtobufDeserializationSchema
 * */
public class CustomProtobufFormatFactory implements DeserializationFormatFactory {
    public static final ConfigOption<ProtobufMessageType> EVENT_CLASS =
            ConfigOptions.key("protobuf.eventclass")
                    .enumType(ProtobufMessageType.class)
                    .defaultValue(ProtobufMessageType.USER_ACTIVITY);

    @Override
    public DecodingFormat<DeserializationSchema<RowData>> createDecodingFormat(DynamicTableFactory.Context context, ReadableConfig formatOptions) {
        final ProtobufMessageType protobufMessageType = formatOptions.get(EVENT_CLASS);

        return new CustomProtobufDecodingFormat(protobufMessageType);
    }

    @Override
    public String factoryIdentifier() {
        return "custom-protobuf";
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(EVENT_CLASS);
        return options;
    }
}
