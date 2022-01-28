package com.cosmian.rest.abe;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.cosmian.CosmianException;
import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VendorAttributes {

    public static final String VENDOR_ID_COSMIAN = "cosmian";
    public static final String VENDOR_ATTR_ABE_ATTR = "abe_attributes";
    public static final String VENDOR_ATTR_ABE_POLICY = "abe_policy";
    public static final String VENDOR_ATTR_ABE_ACCESS_POLICY = "abe_access_policy";
    public static final String VENDOR_ATTR_ABE_HEADER_UID = "abe_header_uid";
    public static final String VENDOR_ATTR_ABE_MASTER_PRIV_KEY_ID = "abe_master_private_key_id";
    public static final String VENDOR_ATTR_ABE_MASTER_PUB_KEY_ID = "abe_master_public_key_id";

    public static VendorAttribute abeAttributesAsVendorAttribute(Attr[] abePolicyAttributes)
            throws CosmianException {
        // The value must be the JSON array of the String representation of the Attrs
        ArrayList<String> array = new ArrayList<String>();
        for (Attr attr : abePolicyAttributes) {
            array.add(attr.toString());
        }
        ObjectMapper mapper = new ObjectMapper();
        byte[] value;
        try {
            value = mapper.writeValueAsBytes(array.toArray());
        } catch (JsonProcessingException e) {
            throw new CosmianException("Failed serializing to JSON the  ABE attributes: " + e.getMessage(),
                    e);
        }
        return new VendorAttribute(VENDOR_ID_COSMIAN, VENDOR_ATTR_ABE_ATTR, value);
    }

    public static Policy policyFromVendorAttributes(Attributes attributes) throws CosmianException {
        VendorAttribute[] vas;
        if (attributes.getVendorAttributes().isPresent()) {
            vas = attributes.getVendorAttributes().get();
        } else {
            throw new CosmianException("No policy available in the attributes: no vendor attributes");
        }
        for (VendorAttribute va : vas) {
            if (va.getVendor_identification().equals(VENDOR_ID_COSMIAN)) {
                if (va.getAttribute_name().equals(VENDOR_ATTR_ABE_POLICY)) {
                    String policyJson = new String(va.getAttribute_value(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        Policy policy = mapper.readValue(policyJson, Policy.class);
                        return policy;
                    } catch (Exception e) {
                        throw new CosmianException("Invalid policy JSON: " + policyJson);
                    }
                }
            }
        }
        throw new CosmianException("No policy available in the vendor attributes");
    }
}
