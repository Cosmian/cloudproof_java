package com.cosmian.rest.abe.policy;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A policy group is a set of fixed policy axis, defining an inner attribute
 * element for each policy axis attribute a fixed number of revocation /
 * addition of attributes is allowed
 */
@JsonSerialize(using = PolicyGroupSerializer.class)
public class Policy {
    private int lastAttributeValue = 0;
    private final int maxNumberOfRevocations;
    // store the policies by name
    private HashMap<String, PolicyAxis> store = new HashMap<>();
    // mapping between (policy_name, policy_attribute) -> integer
    private HashMap<PolicyAttributeUid, TreeSet<Integer>> attributeToInt = new HashMap<>();

    /**
     * Instantiate an empty policy allowing the given max number of revocations of
     * attributes
     * 
     * @param maxNumberOfRevocations
     */
    public Policy(int maxNumberOfRevocations) {
        this.maxNumberOfRevocations = maxNumberOfRevocations;
    }

    /**
     * Add the given Axis to this policy and return the policy
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
     * @throws CosmianException
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

    /**
     * The last attribute value that was assigned. This value is usually incremented
     * as attributes are revoked. The value should therefore no exceed the value
     * returned by {@link #getMaxNumberOfRevocations()}
     */
    public int getLastAttributeValue() {
        return this.lastAttributeValue;
    }

    /**
     * @see
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
