package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.apache.commons.codec.binary.Hex;

public class KmipBytesSerializer extends StdSerializer<byte[]> {

    private static final Logger logger = Logger.getLogger(KmipBytesSerializer.class.getName());

    private final String tag;

    public KmipBytesSerializer(String tag) {
        super(byte[].class);
        this.tag = tag;
    }

    @Override
    public void serialize(byte[] value, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer(() -> "Serializing bytes ");

        generator.writeStartObject();
        generator.writeFieldName("tag");
        generator.writeString(tag);
        generator.writeFieldName("type");
        generator.writeString("ByteString");
        generator.writeFieldName("value");
        generator.writeString(Hex.encodeHexString((byte[]) value).toUpperCase());
        generator.writeEndObject();
    }
}
