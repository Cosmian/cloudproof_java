package com.cosmian.rest.abe.policy;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

import com.cosmian.CloudproofException;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A policy group is a set of fixed policy axis, defining an inner attribute element for each policy axis attribute a
 * fixed number of revocation / addition of attributes is allowed
 */
@JsonSerialize(using = PolicySerializer.class)
@JsonDeserialize(using = PolicyDeserializer.class)
public class Policy implements Serializable {
    private int lastAttributeValue = 0;

    private final int maxAttributeCreations;

    // store the policies by name
    private HashMap<String, PolicyAxis> axes = new HashMap<>();

    // mapping between (policy_name, policy_attribute) -> integer
    private HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt = new HashMap<>();

    public Policy(int lastAttributeValue, int maxAttributeCreations, HashMap<String, PolicyAxis> axes,
        HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt) {
        this.lastAttributeValue = lastAttributeValue;
        this.maxAttributeCreations = maxAttributeCreations;
        this.axes = axes;
        this.attributeToInt = attributeToInt;
    }

    /**
     * Instantiate an empty policy allowing up to 2^32 rotation of attributes
     */
    public Policy() {
        this.maxAttributeCreations = Integer.MAX_VALUE;
    }

    /**
     * Instantiate an empty policy allowing the given max number of revocations of attributes
     *
     * @param maxAttributeCreations the maximum number of possible attributes
     */
    public Policy(int maxAttributeCreations) {
        this.maxAttributeCreations = maxAttributeCreations;
    }

    /**
     * Add the given Axis to this policy and return the policy
     *
     * @param name axis name
     * @param attributes policy attributes of the axis
     * @param hierarchical whether the axis is hierarchical
     * @return the update Policy
     * @throws CloudproofException if the addition fails
     */
    public Policy addAxis(String name, String[] attributes, boolean hierarchical) throws CloudproofException {
        PolicyAxis axis = new PolicyAxis(name, attributes, hierarchical);
        if (axis.getLen() + this.lastAttributeValue > this.maxAttributeCreations) {
            throw new CloudproofException("Attribute capacity overflow");
        }
        if (this.axes.get(axis.getName()) != null) {
            throw new CloudproofException("Policy " + axis.getName() + " already exists");
        }
        this.axes.put(axis.getName(), axis);
        for (String attribute : axis.getAttributes()) {
            this.lastAttributeValue += 1;
            this.attributeToInt.put(new PolicyAttributeUid(axis.getName(), attribute),
                new TreeSet<>(Arrays.asList(new Integer[] {this.lastAttributeValue})));
        }
        return this;
    }

    /**
     * Convert the policy to a KMIP Vendor attribute that can be set on a KMIP Object
     *
     * @return the {@link VendorAttribute}
     * @throws CloudproofException if the JSON cannot be serialized
     */
    public VendorAttribute toVendorAttribute() throws CloudproofException {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Failed serializing the Policy to json: " + e.getMessage(), e);
        }
        return new VendorAttribute(VendorAttribute.VENDOR_ID_COSMIAN,
            VendorAttribute.VENDOR_ATTR_COVER_CRYPT_POLICY,
            json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extract the policy from the Key Attributes
     * 
     * @param attributes the key attributes
     * @return the {Policy}
     * @throws CloudproofException if there is no policy in the attributes
     */
    public static Policy fromAttributes(Attributes attributes) throws CloudproofException {
        VendorAttribute[] vas;
        if (attributes.getVendorAttributes().isPresent()) {
            vas = attributes.getVendorAttributes().get();
        } else {
            throw new CloudproofException("No policy available in the attributes: no vendor attributes");
        }
        for (VendorAttribute va : vas) {
            if (va.getVendor_identification().equals(VendorAttribute.VENDOR_ID_COSMIAN)) {
                if (va.getAttribute_name().equals(VendorAttribute.VENDOR_ATTR_ABE_POLICY) || va.getAttribute_name()
                    .equals(VendorAttribute.VENDOR_ATTR_COVER_CRYPT_POLICY)) {
                    String policyJson = new String(va.getAttribute_value(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readValue(policyJson, Policy.class);
                    } catch (Exception e) {
                        throw new CloudproofException("Invalid policy JSON: " + policyJson);
                    }
                }
            }
        }
        throw new CloudproofException("No policy available in the vendor attributes");
    }

    /**
     * The last attribute value that was assigned. This value is usually incremented as attributes are revoked. The
     * value should therefore no exceed the value returned by {@link #getMaxAttributeCreations()}
     *
     * @return the last attribute value
     */
    public int getLastAttributeValue() {
        return this.lastAttributeValue;
    }

    /**
     * @param lastAttributeValue the last attribute value that was assigned.
     */
    void setLastAttributeValue(int lastAttributeValue) {
        this.lastAttributeValue = lastAttributeValue;
    }

    public int getMaxAttributeCreations() {
        return this.maxAttributeCreations;
    }

    public HashMap<String, PolicyAxis> getAxes() {
        return this.axes;
    }

    void setAxes(HashMap<String, PolicyAxis> axes) {
        this.axes = axes;
    }

    public HashMap<PolicyAttributeUid, TreeSet<Integer>> getAttributeToInt() {
        return this.attributeToInt;
    }

    void setAttributeToInt(HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt) {
        this.attributeToInt = attributeToInt;
    }

    public Policy lastAttribute(int lastAttribute) {
        setLastAttributeValue(lastAttribute);
        return this;
    }

    Policy axes(HashMap<String, PolicyAxis> axes) {
        setAxes(axes);
        return this;
    }

    Policy attributeToInt(HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt) {
        setAttributeToInt(attributeToInt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Policy)) {
            return false;
        }
        Policy policyGroup = (Policy) o;
        return lastAttributeValue == policyGroup.lastAttributeValue
            && maxAttributeCreations == policyGroup.maxAttributeCreations && Objects.equals(axes, policyGroup.axes)
            && Objects.equals(attributeToInt, policyGroup.attributeToInt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastAttributeValue, maxAttributeCreations, axes, attributeToInt);
    }

    @Override
    public String toString() {
        return "{" + " last Attribute='" + getLastAttributeValue() + "'" + ", maxAttributeCreations='"
            + getMaxAttributeCreations() + "'" + ", axes='" + getAxes() + "'" + ", attributeToInt='"
            + getAttributeToInt() + "'" + "}";
    }

}
