package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipEnumDeserializer extends KmipJsonDeserializer<Enum<?>> {

    private static final Logger logger = Logger.getLogger(KmipEnumDeserializer.class.getName());

    private final Class<?> handled_type;

    public KmipEnumDeserializer(Class<Enum<?>> handled_type) {
        this.handled_type = handled_type;
    }

    @Override
    public Class<?> handledType() {
        return this.handled_type;
    }

    @Override
    public Enum<?> deserialize(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        // recover instance from tag
        JsonNode tag_node = node.get("tag");
        if (tag_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No tag");
        }

        @SuppressWarnings("unchecked")
        Class<Enum<?>> clazz = (Class<Enum<?>>) this.handledType();

        logger.finer("Deserializing a " + clazz.getName());

        // check it is TextString
        JsonNode type_node = node.get("type");
        if (type_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No type");
        }
        if (!type_node.asText().equals("Enumeration")) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". Not an Enumeration");
        }
        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }
        String value = value_node.asText();
        ObjectType.values();

        Enum<?> o;
        try {
            Method method = clazz.getMethod("from", String.class);
            o = (Enum<?>) method.invoke(null, value);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
            throw new IOException("Not a KMIP Enumeration " + tag_node.asText() + ": " + e.getMessage(), e);
        }

        return o;
    }

}
