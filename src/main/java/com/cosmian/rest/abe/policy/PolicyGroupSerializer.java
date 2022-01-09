package com.cosmian.rest.abe.policy;

import java.io.IOException;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PolicyGroupSerializer extends JsonSerializer<Policy> {

    @Override
    public void serialize(Policy policyGroup, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {

        generator.writeStartObject();
        generator.writeFieldName("last_attribute");
        generator.writeNumber(policyGroup.getLastAttributeValue());
        generator.writeFieldName("max_attribute");
        generator.writeNumber(policyGroup.getMaxNumberOfRevocations());
        // store
        generator.writeFieldName("store");
        generator.writeStartObject();
        for (Entry<String, PolicyAxis> entry : policyGroup.getStore().entrySet()) {
            generator.writeFieldName(entry.getKey());
            generator.writeStartArray();
            generator.writeStartArray();
            for (String attribute : entry.getValue().getAttributes()) {
                generator.writeString(attribute);
            }
            generator.writeEndArray();
            generator.writeBoolean(entry.getValue().isHierarchical());
            generator.writeEndArray();
        }
        generator.writeEndObject();
        // attribute_to_int
        generator.writeFieldName("attribute_to_int");
        generator.writeStartObject();
        for (Entry<PolicyAttributeUid, TreeSet<Integer>> entry : policyGroup.getAttributeToInt().entrySet()) {
            generator.writeFieldName(entry.getKey().toString());
            generator.writeStartArray();
            for (int value : entry.getValue()) {
                generator.writeNumber(value);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }

}
/**
 * VendorAttribute { vendor_identification: "cosmian".to_owned(),
 * attribute_name: "abe_policy".to_owned(), attribute_value:
 * serde_json::to_vec(pg) .context("failed serializing the ABE policy group
 * value") .reason(ErrorReason::Invalid_Attribute_Value)?, }
 */