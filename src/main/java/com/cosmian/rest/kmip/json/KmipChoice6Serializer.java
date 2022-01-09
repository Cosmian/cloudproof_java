package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class KmipChoice6Serializer extends JsonSerializer<KmipChoice6<?, ?, ?, ?, ?, ?>> {

    private static final Logger logger = Logger.getLogger(KmipChoice6Serializer.class.getName());

    private final String tag;

    public KmipChoice6Serializer(String tag) {
        this.tag = tag;
    }

    @Override
    public void serialize(KmipChoice6<?, ?, ?, ?, ?, ?> value, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {

        logger.finer("Serializing a " + value.getClass().toString());
        KmipJson.serialize_value(tag, value.get(), generator, serializers);

    }
}
