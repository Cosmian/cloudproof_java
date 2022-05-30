package com.cosmian.rest.cover_crypt.acccess_policy;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class AndSerializer extends JsonSerializer<And> {

    private static final Logger logger = Logger.getLogger(AndSerializer.class.getName());

    @Override
    public void serialize(And and, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer(() -> "Serializing a " + and.getClass().toString());

        generator.writeStartObject();
        generator.writeFieldName("And");
        generator.writeStartArray();
        generator.writeObject(and.getLeft());
        generator.writeObject(and.getRight());
        generator.writeEndArray();
        generator.writeEndObject();
    }
}
