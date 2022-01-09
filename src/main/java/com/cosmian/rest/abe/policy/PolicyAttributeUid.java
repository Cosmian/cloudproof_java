package com.cosmian.rest.abe.policy;

import java.util.Objects;

/**
 * An attribute in a policy group is characterized by the policy axis and its
 * own name within that axis
 */
public class PolicyAttributeUid {
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

}
