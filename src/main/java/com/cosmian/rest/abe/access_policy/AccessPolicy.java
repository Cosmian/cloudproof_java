package com.cosmian.rest.abe.access_policy;

import java.nio.charset.StandardCharsets;

import com.cosmian.CloudproofException;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessPolicy {

    public VendorAttribute toVendorAttribute() throws CloudproofException {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Failed serializing the Access Policy to json: " + e.getMessage(), e);
        }
        return new VendorAttribute(VendorAttribute.VENDOR_ID_COSMIAN,
            VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ACCESS_POLICY,
            json.getBytes(StandardCharsets.UTF_8));
    }
}
