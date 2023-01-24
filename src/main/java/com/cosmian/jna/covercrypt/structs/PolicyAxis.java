package com.cosmian.jna.covercrypt.structs;

import java.io.Serializable;
import java.util.Objects;

public class PolicyAxis implements Serializable {
    final private String name;

    final private PolicyAxisAttribute[] attributes;

    final private boolean hierarchical;

    public PolicyAxis(String name, PolicyAxisAttribute[] attributes, boolean hierarchical) {
        this.name = name;
        this.attributes = attributes;
        this.hierarchical = hierarchical;
    }

    public int getLen() {
        return attributes.length;
    }

    public String getName() {
        return this.name;
    }

    public String getAxisAttributeProperties() {
        String attributes = "[";
        for (int i = 0; i < this.attributes.length; i++) {
            attributes += this.attributes[i].toString();
            if (i != this.attributes.length - 1) {
                attributes += ", ";
            }
        }
        attributes += "]";
        return attributes;
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
        return "{ \"name\": \"" + getName() + "\", \"attributes_properties\": " + getAxisAttributeProperties()
            + ", \"hierarchical\": "
            + isHierarchical() + " }";
    }

}
