package com.cosmian.rest.gpsw.policy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PolicyAxis implements Serializable {
    final private String name;

    final private List<String> attributes;

    final private boolean hierarchical;

    public PolicyAxis(String name, String[] attributes, boolean hierarchical) {
        this.name = name;
        this.attributes = Arrays.asList(attributes);
        this.hierarchical = hierarchical;
    }

    public int getLen() {
        return this.attributes.size();
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAttributes() {
        return this.attributes;
    }

    public boolean isHierarchical() {
        return this.hierarchical;
    }

    public boolean getHierarchical() {
        return this.hierarchical;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PolicyAxis)) {
            return false;
        }
        PolicyAxis policyAxis = (PolicyAxis) o;
        return Objects.equals(name, policyAxis.name) && Objects.equals(attributes, policyAxis.attributes)
            && hierarchical == policyAxis.hierarchical;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes, hierarchical);
    }

    @Override
    public String toString() {
        return "{" + " name='" + getName() + "'" + ", attributes='" + getAttributes() + "'" + ", hierarchical='"
            + isHierarchical() + "'" + "}";
    }

}
