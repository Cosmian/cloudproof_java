package com.cosmian.rest.abe;

import java.util.ArrayList;

import com.cosmian.CosmianException;
import com.cosmian.rest.abe.acccess_policy.Attr;
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

    public static VendorAttribute abe_attributes_as_vendor_attribute(Attr[] abePolicyAttributes)
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
}
