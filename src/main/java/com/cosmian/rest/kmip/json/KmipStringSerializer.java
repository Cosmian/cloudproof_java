package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class KmipStringSerializer extends StdSerializer<String> {

    private static final Logger logger = Logger.getLogger(KmipStringSerializer.class.getName());

    private final String tag;

    public KmipStringSerializer(String tag) {
        super(String.class);
        this.tag = tag;
    }

    @Override
    public void serialize(String value, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer("Serializing a " + value.getClass().toString());

        generator.writeStartObject();
        generator.writeFieldName("tag");
        generator.writeString(tag);
        generator.writeFieldName("type");
        generator.writeString("TextString");
        generator.writeFieldName("value");
        generator.writeString(value);
        generator.writeEndObject();
    }
}
