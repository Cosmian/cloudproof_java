package com.cosmian.rest.kmip.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A Jackson Deserializer that proposes an additional deserialization method
 */
public abstract class KmipJsonDeserializer<E> extends JsonDeserializer<E> {

    @Override
    public E deserialize(JsonParser p, DeserializationContext context) throws IOException, JacksonException {
        JsonNode n = p.getCodec().readTree(p);
        return this.deserialize(n, context);
    }

    /**
     * Deserializes an already existing {@link JsonNode}
     *
     * @see #deserialize(JsonNode, DeserializationContext)
     * @param n the {@link JsonNode}
     * @param context a {@link DeserializationContext}
     * @return the deserialized value
     * @throws IOException if the JSON cannot be read
     * @throws JacksonException if the JSON is malformed
     */
    public abstract E deserialize(JsonNode n, DeserializationContext context) throws IOException, JacksonException;
}
