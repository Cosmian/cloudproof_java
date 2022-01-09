package com.cosmian.rest.kmip.json;

import java.io.IOException;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipStringDeserializer extends KmipJsonDeserializer<String> {

    private static final Logger logger = Logger.getLogger(KmipStringDeserializer.class.getName());

    @Override
    public String deserialize(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        logger.finer("Deserializing a KmipString");

        // check it is TextString
        JsonNode type_node = node.get("type");
        if (type_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No type");
        }
        if (!type_node.asText().equals("TextString")) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". Not a TextString");
        }
        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }
        return value_node.asText();
    }

}
