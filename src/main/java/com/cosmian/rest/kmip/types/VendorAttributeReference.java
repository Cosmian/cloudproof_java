package com.cosmian.rest.kmip.types;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VendorAttributeReference {

    /**
     * Text String (with usage limited to alphanumeric, underscore and period – i.e.
     * [A-Za-z0-9_.])
     */
    @JsonProperty("VendorIdentification")
    private String vendor_identification;

    @JsonProperty("AttributeName")
    private String attribute_name;

    public static VendorAttribute empty() {
        return new VendorAttribute();
    }

    public VendorAttributeReference() {
    }

    public VendorAttributeReference(String vendor_identification, String attribute_name) {
        this.vendor_identification = vendor_identification;
        this.attribute_name = attribute_name;
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

    public VendorAttributeReference vendor_identification(String vendor_identification) {
        setVendor_identification(vendor_identification);
        return this;
    }

    public VendorAttributeReference attribute_name(String attribute_name) {
        setAttribute_name(attribute_name);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VendorAttributeReference)) {
            return false;
        }
        VendorAttributeReference vendorAttributeReference = (VendorAttributeReference) o;
        return Objects.equals(vendor_identification, vendorAttributeReference.vendor_identification)
                && Objects.equals(attribute_name, vendorAttributeReference.attribute_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor_identification, attribute_name);
    }

    @Override
    public String toString() {
        return "{" + " vendor_identification='" + getVendor_identification() + "'" + ", attribute_name='"
                + getAttribute_name() + "'" + "}";
    }

}
