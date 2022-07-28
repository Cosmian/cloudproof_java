package com.cosmian.rest.abe.access_policy;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OrSerializer extends JsonSerializer<Or> {

    private static final Logger logger = Logger.getLogger(OrSerializer.class.getName());

    @Override
    public void serialize(Or and, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer(() -> "Serializing a " + and.getClass().toString());

        generator.writeStartObject();
        generator.writeFieldName("Or");
        generator.writeStartArray();
        generator.writeObject(and.getLeft());
        generator.writeObject(and.getRight());
        generator.writeEndArray();
        generator.writeEndObject();
    }
}
