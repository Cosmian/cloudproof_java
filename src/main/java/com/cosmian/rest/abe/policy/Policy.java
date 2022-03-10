package com.cosmian.rest.abe.policy;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A policy group is a set of fixed policy axis, defining an inner attribute
 * element for each policy axis attribute a fixed number of revocation /
 * addition of attributes is allowed
 */
@JsonSerialize(using = PolicySerializer.class)
@JsonDeserialize(using = PolicyDeserializer.class)
public class Policy implements Serializable {
    private int lastAttributeValue = 0;
    private final int maxNumberOfRevocations;
    // store the policies by name
    private HashMap<String, PolicyAxis> store = new HashMap<>();
    // mapping between (policy_name, policy_attribute) -> integer
    private HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt = new HashMap<>();

    public Policy(int lastAttributeValue, int maxNumberOfRevocations, HashMap<String, PolicyAxis> store,
            HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt) {
        this.lastAttributeValue = lastAttributeValue;
        this.maxNumberOfRevocations = maxNumberOfRevocations;
        this.store = store;
        this.attributeToInt = attributeToInt;
    }

    /**
     * Instantiate an empty policy allowing the given max number of revocations of
     * attributes
     * 
     * @param maxNumberOfRevocations the maximum number of attributes revocations
     */
    public Policy(int maxNumberOfRevocations) {
        this.maxNumberOfRevocations = maxNumberOfRevocations;
    }

    /**
     * Add the given Axis to this policy and return the policy
     * 
     * @param name         axis name
     * @param attributes   policy attributes of the axis
     * @param hierarchical whether the axis is hierarchical
     * @return the update Policy
     * @throws CosmianException if the addition fails
     */
    public Policy addAxis(String name, String[] attributes, boolean hierarchical) throws CosmianException {
        PolicyAxis axis = new PolicyAxis(name, attributes, hierarchical);
        if (axis.getLen() + this.lastAttributeValue > this.maxNumberOfRevocations) {
            throw new CosmianException("Attribute capacity overflow");
        }
        if (this.store.get(axis.getName()) != null) {
            throw new CosmianException("Policy " + axis.getName() + " already exists");
        }
        this.store.put(axis.getName(), axis);
        for (String attribute : axis.getAttributes()) {
            this.lastAttributeValue += 1;
            this.attributeToInt.put(new PolicyAttributeUid(axis.getName(), attribute),
                    new TreeSet<>(Arrays.asList(new Integer[] { this.lastAttributeValue })));
        }
        return this;
    }

    /**
     * Convert the policy to a KMIP Vendor attribute that can be set on a KMIP
     * Object
     * 
     * @return the {@link VendorAttribute}
     * @throws CosmianException if the JSON cannot be serialized
     */
    public VendorAttribute toVendorAttribute() throws CosmianException {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CosmianException("Failed serializing the Policy to json: " + e.getMessage(), e);
        }
        return new VendorAttribute("cosmian", "abe_policy", json.getBytes(StandardCharsets.UTF_8));
    }

    public static Policy fromVendorAttributes(Attributes attributes) throws CosmianException {
        VendorAttribute[] vas;
        if (attributes.getVendorAttributes().isPresent()) {
            vas = attributes.getVendorAttributes().get();
        } else {
            throw new CosmianException("No policy available in the attributes: no vendor attributes");
        }
        for (VendorAttribute va : vas) {
            if (va.getVendor_identification().equals(VendorAttribute.VENDOR_ID_COSMIAN)) {
                if (va.getAttribute_name().equals(VendorAttribute.VENDOR_ATTR_ABE_POLICY)) {
                    String policyJson = new String(va.getAttribute_value(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readValue(policyJson, Policy.class);
                    } catch (Exception e) {
                        throw new CosmianException("Invalid policy JSON: " + policyJson);
                    }
                }
            }
        }
        throw new CosmianException("No policy available in the vendor attributes");
    }

    /**
     * The last attribute value that was assigned. This value is usually incremented
     * as attributes are revoked. The value should therefore no exceed the value
     * returned by {@link #getMaxNumberOfRevocations()}
     * 
     * @return the last attribute value
     */
    public int getLastAttributeValue() {
        return this.lastAttributeValue;
    }

    /**
     * @param lastAttributeValue
     */
    void setLastAttributeValue(int lastAttributeValue) {
        this.lastAttributeValue = lastAttributeValue;
    }

    public int getMaxNumberOfRevocations() {
        return this.maxNumberOfRevocations;
    }

    public HashMap<String, PolicyAxis> getStore() {
        return this.store;
    }

    void setStore(HashMap<String, PolicyAxis> store) {
        this.store = store;
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

    Policy store(HashMap<String, PolicyAxis> store) {
        setStore(store);
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
                && maxNumberOfRevocations == policyGroup.maxNumberOfRevocations
                && Objects.equals(store, policyGroup.store)
                && Objects.equals(attributeToInt, policyGroup.attributeToInt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastAttributeValue, maxNumberOfRevocations, store, attributeToInt);
    }

    @Override
    public String toString() {
        return "{" + " lastAttribute='" + getLastAttributeValue() + "'" + ", maxNumberOfRevocations='"
                + getMaxNumberOfRevocations() + "'" + ", store='" + getStore() + "'" + ", attributeToInt='"
                + getAttributeToInt() + "'" + "}";
    }

}
