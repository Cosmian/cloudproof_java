package com.cosmian.rest.abe.policy;

import java.io.Serializable;
import java.util.Objects;

import com.cosmian.utils.CloudproofException;

/**
 * An attribute in a policy group is characterized by the policy axis and its own name within that axis
 */
public class PolicyAttributeUid implements Serializable {
    private final String axis;

    private final String name;

    public PolicyAttributeUid(String axis, String name) {
        this.axis = axis;
        this.name = name;
    }

    public String getAxis() {
        return this.axis;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PolicyAttributeUid)) {
            return false;
        }
        PolicyAttributeUid policyAttribute = (PolicyAttributeUid) o;
        return Objects.equals(axis, policyAttribute.axis) && Objects.equals(name, policyAttribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(axis, name);
    }

    @Override
    public String toString() {
        return getAxis() + "::" + getName();
    }

    public static PolicyAttributeUid fromString(String s) throws CloudproofException {
        String[] parts = s.split("::");
        if (parts.length != 2) {
            throw new CloudproofException("Invalid Policy attribute string");
        }
        return new PolicyAttributeUid(parts[0], parts[1]);
    }

}
