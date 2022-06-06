package com.cosmian.rest.kmip.json;

import java.io.IOException;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipBytesDeserializer extends KmipJsonDeserializer<byte[]> {

    private static final Logger logger = Logger.getLogger(KmipBytesDeserializer.class.getName());

    @Override
    public byte[] deserialize(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        logger.finer("Deserializing bytes");

        // check it is TextString
        JsonNode type_node = node.get("type");
        if (type_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No type");
        }
        if (!type_node.asText().equals("ByteString")) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". Not a ByteString");
        }
        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }
        try {
            // conversion to char array to make the library compatible with older versions of commons-codec
            // such as the ones found in spark-2.7
            return Hex.decodeHex(value_node.asText().toLowerCase().toCharArray());
        } catch (DecoderException e) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". Value is no a ByteString");
        }
    }

}
