package com.cosmian.jna.covercrypt.structs;

import java.nio.charset.StandardCharsets;

import com.cosmian.rest.kmip.types.VendorAttribute;
import com.cosmian.utils.CloudproofException;

public class AccessPolicy extends Ffi {
    private String _booleanExpression;

    public AccessPolicy(String booleanExpression) throws CloudproofException {
        unwrap(INSTANCE.h_validate_boolean_expression(booleanExpression));
        _booleanExpression = booleanExpression;
    }

    public VendorAttribute toVendorAttribute() throws CloudproofException {
        return new VendorAttribute(
            VendorAttribute.VENDOR_ID_COSMIAN,
            VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ACCESS_POLICY,
            this.getBytes());
    }

    /*
     * Return the UTF-8 encoding of the access policy string.
     */
    public byte[] getBytes() {
        return _booleanExpression.getBytes(StandardCharsets.UTF_8);
    }
}
