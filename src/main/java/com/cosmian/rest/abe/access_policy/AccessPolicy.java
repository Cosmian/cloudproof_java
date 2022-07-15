package com.cosmian.rest.abe.access_policy;

import java.nio.charset.StandardCharsets;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessPolicy {

    public VendorAttribute toVendorAttribute(String abeImplementation) throws CosmianException {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CosmianException("Failed serializing the Access Policy to json: " + e.getMessage(), e);
        }
        return new VendorAttribute(VendorAttribute.VENDOR_ID_COSMIAN, abeImplementation,
            json.getBytes(StandardCharsets.UTF_8));
    }
}
