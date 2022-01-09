package com.cosmian.rest.kmip.json;

import java.io.IOException;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipIntegerDeserializer extends KmipJsonDeserializer<Integer> {

    private static final Logger logger = Logger.getLogger(KmipIntegerDeserializer.class.getName());

    @Override
    public Integer deserialize(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        logger.finer("Deserializing a KmipInteger");

        // check it is a Integer
        JsonNode type_node = node.get("type");
        if (type_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No type");
        }
        if (!type_node.asText().equals("Integer")) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". Not a Integer");
        }
        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }
        if (value_node.canConvertToInt()) {
            return value_node.asInt();
        }
        String hex = value_node.asText();
        if (hex.startsWith("0x")) {
            return (int) Long.parseLong(hex.substring(2), 16);
        }
        throw new IOException("Invalid Integer " + value_node);
    }

}
