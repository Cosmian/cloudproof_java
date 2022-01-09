package com.cosmian.rest.kmip.json;

import java.io.IOException;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipBooleanDeserializer extends KmipJsonDeserializer<Boolean> {

    private static final Logger logger = Logger.getLogger(KmipBooleanDeserializer.class.getName());

    @Override
    public Boolean deserialize(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        logger.finer("Deserializing a KmipBoolean");

        // check it is a Boolean
        JsonNode type_node = node.get("type");
        if (type_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No type");
        }
        if (!type_node.asText().equals("Boolean")) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". Not a Boolean");
        }
        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }
        return value_node.asBoolean();
    }

}
