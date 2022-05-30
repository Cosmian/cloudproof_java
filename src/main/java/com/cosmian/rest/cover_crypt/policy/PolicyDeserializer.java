package com.cosmian.rest.cover_crypt.policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.json.KmipJsonDeserializer;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class PolicyDeserializer extends KmipJsonDeserializer<Policy> {

    @Override
    public Policy deserialize(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        JsonNode lastAttributeValueNode = node.get("last_attribute_value");
        if (lastAttributeValueNode == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No last_attribute");
        }
        int lastAttributeValue = lastAttributeValueNode.asInt();

        JsonNode maxAttributeValueNode = node.get("max_attribute_value");
        if (maxAttributeValueNode == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No max_attribute");
        }
        int maxAttributeValue = maxAttributeValueNode.asInt();

        JsonNode storeNode = node.get("store");
        if (storeNode == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No store");
        }

        // PolicyAxis has a non standard deserialization
        // ==> do it manually
        HashMap<String, PolicyAxis> store = new HashMap<>();
        for (Entry<String, JsonNode> entry : toIterable(storeNode.fields())) {

            JsonNode axisNode = entry.getValue();
            ArrayList<String> values = new ArrayList<>();
            for (JsonNode value : toIterable(axisNode.get(0).elements())) {
                values.add(value.asText());
            }
            PolicyAxis axis =
                new PolicyAxis(entry.getKey(), values.toArray(new String[values.size()]), axisNode.get(1).asBoolean());
            store.put(entry.getKey(), axis);
        }

        // PolicyAttributeUid has a non standard Json deserialization
        JsonNode attributeToIntNode = node.get("attribute_to_int");
        HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt = new HashMap<>();
        for (Entry<String, JsonNode> entry : toIterable(attributeToIntNode.fields())) {
            TreeSet<Integer> set = new TreeSet<>();
            for (JsonNode jsonNode : toIterable(entry.getValue().elements())) {
                set.add(jsonNode.asInt());
            }
            try {
                attributeToInt.put(PolicyAttributeUid.fromString(entry.getKey()), set);
            } catch (CosmianException e) {
                throw new IOException(e);
            }
        }

        return new Policy(lastAttributeValue, maxAttributeValue, store, attributeToInt);
    }

    static <T> Iterable<T> toIterable(Iterator<T> it) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return it;
            }

        };
    }

}
