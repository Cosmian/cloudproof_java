package com.cosmian.rest.kmip.types;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A vendor specific Attribute is a structure used for sending and receiving a Managed Object attribute. The Vendor
 * Identification and Attribute Name are text-strings that are used to identify the attribute. The Attribute Value is
 * either a primitive data type or structured object, depending on the attribute. Vendor identification values “x” and
 * “y” are reserved for KMIP v2.0 and later implementations referencing KMIP v1.x Custom Attributes.
 *
 * Vendor Attributes created by the client with Vendor Identification “x” are not created (provided during object
 * creation), set, added, adjusted, modified or deleted by the server.
 *
 * Vendor Attributes created by the server with Vendor Identification “y” are not created (provided during object
 * creation), set, added, adjusted, modified or deleted by the client.
 */
public class VendorAttribute implements KmipStruct {

    @JsonIgnore
    public static final String VENDOR_ID_COSMIAN = "cosmian";

    @JsonIgnore
    public static final String VENDOR_ATTR_ABE_ATTR = "abe_attributes";
    @JsonIgnore
    public static final String VENDOR_ATTR_ABE_POLICY = "abe_policy";
    @JsonIgnore
    public static final String VENDOR_ATTR_ABE_ACCESS_POLICY = "abe_access_policy";

    @JsonIgnore
    public static final String VENDOR_ATTR_COVER_CRYPT_ATTR = "cover_crypt_attributes";
    @JsonIgnore
    public static final String VENDOR_ATTR_COVER_CRYPT_POLICY = "cover_crypt_policy";
    @JsonIgnore
    public static final String VENDOR_ATTR_COVER_CRYPT_ACCESS_POLICY = "cover_crypt_access_policy";

    @JsonIgnore
    @Deprecated
    public static final String VENDOR_ATTR_ABE_HEADER_UID = "abe_header_uid";

    /**
     * Text String (with usage limited to alphanumeric, underscore and period – i.e. [A-Za-z0-9_.])
     */
    @JsonProperty("VendorIdentification")
    private String vendor_identification;

    @JsonProperty("AttributeName")
    private String attribute_name;

    @JsonProperty("AttributeValue")
    private byte[] attribute_value;

    public static VendorAttribute empty() {
        return new VendorAttribute();
    }

    public VendorAttribute() {}

    public VendorAttribute(String vendor_identification, String attribute_name, byte[] attribute_value) {
        this.vendor_identification = vendor_identification;
        this.attribute_name = attribute_name;
        this.attribute_value = attribute_value;
    }

    public String getVendor_identification() {
        return this.vendor_identification;
    }

    public void setVendor_identification(String vendor_identification) {
        this.vendor_identification = vendor_identification;
    }

    public String getAttribute_name() {
        return this.attribute_name;
    }

    public void setAttribute_name(String attribute_name) {
        this.attribute_name = attribute_name;
    }

    public byte[] getAttribute_value() {
        return this.attribute_value;
    }

    public void setAttribute_value(byte[] attribute_value) {
        this.attribute_value = attribute_value;
    }

    public VendorAttribute vendor_identification(String vendor_identification) {
        setVendor_identification(vendor_identification);
        return this;
    }

    public VendorAttribute attribute_name(String attribute_name) {
        setAttribute_name(attribute_name);
        return this;
    }

    public VendorAttribute attribute_value(byte[] attribute_value) {
        setAttribute_value(attribute_value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VendorAttribute)) {
            return false;
        }
        VendorAttribute vendorAttribute = (VendorAttribute)o;
        return Objects.equals(vendor_identification, vendorAttribute.vendor_identification)
            && Objects.equals(attribute_name, vendorAttribute.attribute_name)
            && Arrays.equals(attribute_value, vendorAttribute.attribute_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor_identification, attribute_name, attribute_value);
    }

    @Override
    public String toString() {
        return "{" + " vendor_identification='" + getVendor_identification() + "'" + ", attribute_name='"
            + getAttribute_name() + "'" + ", attribute_value='" + Arrays.toString(getAttribute_value()) + "'" + "}";
    }

}
