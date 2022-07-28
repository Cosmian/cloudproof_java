package com.cosmian.rest.abe.access_policy;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class AttrSerializer extends JsonSerializer<Attr> {

    private static final Logger logger = Logger.getLogger(AttrSerializer.class.getName());

    @Override
    public void serialize(Attr attr, JsonGenerator generator, SerializerProvider serializers) throws IOException {

        logger.finer(() -> "Serializing a " + attr.getClass().toString());

        generator.writeStartObject();
        generator.writeFieldName("Attr");
        generator.writeString(attr.axis + "::" + attr.name);
        generator.writeEndObject();
    }
}
