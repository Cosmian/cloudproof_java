package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class KmipArraySerializer<E> extends JsonSerializer<E[]> {

    private static final Logger logger = Logger.getLogger(KmipStructSerializer.class.getName());

    private final String tag;

    public KmipArraySerializer(String tag) {
        this.tag = tag;
    }

    @Override
    public void serialize(E[] array, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        Class<?> clazz = array.getClass().getComponentType();

        generator.writeStartObject();
        generator.writeFieldName("tag");
        generator.writeString(this.tag == null ? clazz.getSimpleName() : this.tag);
        generator.writeFieldName("value");
        generator.writeStartArray();

        logger.finer(() -> "Serializing an array of " + clazz.getName());

        for (E element : array) {
            KmipJson.serialize_value(tag, element, generator, serializers);
        }
        generator.writeEndArray();
        generator.writeEndObject();

    }

}
