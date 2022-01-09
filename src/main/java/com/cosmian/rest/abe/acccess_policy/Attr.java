package com.cosmian.rest.abe.acccess_policy;

import java.util.Objects;

import com.cosmian.CosmianException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = AttrSerializer.class)
public class Attr extends AccessPolicy {

    String axis;
    String name;

    public Attr() {
    }

    private static void assertValidCharacters(String value) throws CosmianException {
        if (value.equals("")) {
            throw new CosmianException("Attributes axes and names cannot be empty strings");
        }
        if (value.startsWith(" ") || value.endsWith(" ")) {
            throw new CosmianException("Attributes axes and names cannot start or end with spaces");
        }
        if (value.contains("::")) {
            throw new CosmianException("Attributes axes and names cannot contain the sequence \"::\"");
        }
    }

    public Attr(String axis, String name) throws CosmianException {
        assertValidCharacters(axis);
        assertValidCharacters(name);
        this.axis = axis;
        this.name = name;
    }

    public String getAxis() {
        return this.axis;
    }

    public void setAxis(String axis) throws CosmianException {
        assertValidCharacters(axis);
        this.axis = axis;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) throws CosmianException {
        assertValidCharacters(name);
        this.name = name;
    }

    public Attr axis(String axis) throws CosmianException {
        setAxis(axis);
        return this;
    }

    public Attr name(String name) throws CosmianException {
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

    public static Attr fromString(String attrString) throws CosmianException {
        String[] parts = attrString.split("::");
        if (parts.length != 2) {
            throw new CosmianException("Invalid attribute string; it should be of the form AXIS::ATTRIBUTE_NAME");
        }
        String axis = parts[0];
        assertValidCharacters(axis);
        String name = parts[1];
        assertValidCharacters(name);
        return new Attr(axis, name);
    }

}
