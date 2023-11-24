package com.cosmian.jna.covercrypt.structs;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.cosmian.rest.kmip.types.VendorAttribute;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Attribute extends Ffi {
    private String _attribute;

    public Attribute(String attribute) throws CloudproofException {
        attribute += "\0";
        unwrap(INSTANCE.h_validate_boolean_expression(attribute));
        _attribute = attribute;
    }

    /*
     * Return the `VendorAttribute` containing the given policy attributes.
     */
    public static VendorAttribute toVendorAttribute(String[] attributes) throws CloudproofException {
        // The value must be the JSON array of the String representation of the Attrs
        ArrayList<String> array = new ArrayList<String>();
        for (String attr : attributes) {
            array.add(attr.toString());
        }
        ObjectMapper mapper = new ObjectMapper();
        byte[] value;
        try {
            value = mapper.writeValueAsBytes(array.toArray());
        } catch (JsonProcessingException e) {
            throw new CloudproofException(
                "Failed serializing to JSON the attributes: " + e.getMessage(), e);
        }
        return new VendorAttribute(
            VendorAttribute.VENDOR_ID_COSMIAN,
            VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ATTR,
            value);
    }

    /*
     * Return the UTF-8 encoding of the access policy string.
     */
    public byte[] getBytes() {
        return _attribute.getBytes(StandardCharsets.UTF_8);
    }
}
