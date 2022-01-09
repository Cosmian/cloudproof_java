package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class KmipIntegerSerializer extends StdSerializer<Integer> {

    private static final Logger logger = Logger.getLogger(KmipIntegerSerializer.class.getName());

    private final String tag;

    public KmipIntegerSerializer(String tag) {
        super(Integer.class);
        this.tag = tag;
    }

    @Override
    public void serialize(Integer value, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer(() -> "Serializing a " + value.getClass().toString());

        generator.writeStartObject();
        generator.writeFieldName("tag");
        generator.writeString(tag);
        generator.writeFieldName("type");
        generator.writeString("Integer");
        generator.writeFieldName("value");
        generator.writeNumber(value);
        generator.writeEndObject();
    }
}
