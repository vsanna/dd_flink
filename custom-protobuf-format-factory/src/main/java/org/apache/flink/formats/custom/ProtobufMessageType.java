package org.apache.flink.formats.custom;

public enum ProtobufMessageType {
    USER_ACTIVITY(0),
    USER_PROFILE(1);

    // position of the messagee in .proto file
    final int positionInProtoFile;

    ProtobufMessageType(int positionInProtoFile) {
        this.positionInProtoFile = positionInProtoFile;
    }
}
