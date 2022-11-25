package com.cosmian.rest.abe.access_policy;

import java.util.ArrayList;
import java.util.Objects;

import com.cosmian.CloudproofException;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = AttrSerializer.class)
public class Attr extends AccessPolicy {

    String axis;

    String name;

    public Attr() {
    }

    private static void assertValidCharacters(String value) throws CloudproofException {
        if (value.equals("")) {
            throw new CloudproofException("Attributes axes and names cannot be empty strings");
        }
        if (value.startsWith(" ") || value.endsWith(" ")) {
            throw new CloudproofException("Attributes axes and names cannot start or end with spaces");
        }
        if (value.contains("::")) {
            throw new CloudproofException("Attributes axes and names cannot contain the sequence \"::\"");
        }
    }

    public Attr(String axis, String name) throws CloudproofException {
        assertValidCharacters(axis);
        assertValidCharacters(name);
        this.axis = axis;
        this.name = name;
    }

    public String getAxis() {
        return this.axis;
    }

    public void setAxis(String axis) throws CloudproofException {
        assertValidCharacters(axis);
        this.axis = axis;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) throws CloudproofException {
        assertValidCharacters(name);
        this.name = name;
    }

    public Attr axis(String axis) throws CloudproofException {
        setAxis(axis);
        return this;
    }

    public Attr name(String name) throws CloudproofException {
        setName(name);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Attr)) {
            return false;
        }
        Attr attr = (Attr) o;
        return Objects.equals(axis, attr.axis) && Objects.equals(name, attr.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(axis, name);
    }

    @Override
    public String toString() {
        return getAxis() + "::" + getName();
    }

    public static Attr fromString(String attrString) throws CloudproofException {
        String[] parts = attrString.split("::");
        if (parts.length != 2) {
            throw new CloudproofException("Invalid attribute string; it should be of the form AXIS::ATTRIBUTE_NAME");
        }
        String axis = parts[0];
        assertValidCharacters(axis);
        String name = parts[1];
        assertValidCharacters(name);
        return new Attr(axis, name);
    }

    public static VendorAttribute toVendorAttribute(Attr[] policyAttributes, String vendor_attribute_abe)
        throws CloudproofException {
        // The value must be the JSON array of the String representation of the Attrs
        ArrayList<String> array = new ArrayList<String>();
        for (Attr attr : policyAttributes) {
            array.add(attr.toString());
        }
        ObjectMapper mapper = new ObjectMapper();
        byte[] value;
        try {
            value = mapper.writeValueAsBytes(array.toArray());
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Failed serializing to JSON the attributes: " + e.getMessage(), e);
        }
        return new VendorAttribute(VendorAttribute.VENDOR_ID_COSMIAN, vendor_attribute_abe,
            value);
    }
}
