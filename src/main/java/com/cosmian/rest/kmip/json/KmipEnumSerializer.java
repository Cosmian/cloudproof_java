package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class KmipEnumSerializer extends StdSerializer<Enum<?>> {

    private static final Logger logger = Logger.getLogger(KmipEnumSerializer.class.getName());

    private final String tag;

    public KmipEnumSerializer(Class<Enum<?>> t, String tag) {
        super(t);
        this.tag = tag;
    }

    @Override
    public void serialize(Enum<?> value, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer("Serializing a " + value.getClass().toString());

        generator.writeStartObject();
        generator.writeFieldName("tag");
        generator.writeString(tag);
        generator.writeFieldName("type");
        generator.writeString("Enumeration");
        generator.writeFieldName("value");
        generator.writeString(KmipEnumUtils.to_string(((Enum<?>)value)));
        generator.writeEndObject();
    }
}
