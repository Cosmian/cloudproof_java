package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipArrayDeserializer<E> extends KmipJsonDeserializer<E[]> {

    private static final Logger logger = Logger.getLogger(KmipArrayDeserializer.class.getName());

    private final Class<?> handled_type;

    public KmipArrayDeserializer(Class<E[]> handled_type) {
        this.handled_type = handled_type;
    }

    @Override
    public Class<?> handledType() {
        return this.handled_type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E[] deserialize(JsonNode node, DeserializationContext context) throws IOException, JacksonException {

        // recover instance from tag
        JsonNode tag_node = node.get("tag");
        if (tag_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No tag");
        }
        Class<?> element_class = this.handledType().getComponentType();
        logger.finer(() -> "Deserializing an array of " + element_class.getName());

        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }

        ArrayList<Object> list = new ArrayList<Object>();
        for (JsonNode element_node : value_node) {
            Object element = KmipJson.deserialize_value(element_class, element_node, context);
            list.add(element);
        }

        E[] array = (E[]) Array.newInstance(element_class, list.size());
        for (int i = 0; i < array.length; i++) {
            array[i] = (E) list.get(i);
        }

        return array;
    }
}
