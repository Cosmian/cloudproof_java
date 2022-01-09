package com.cosmian.rest.kmip.data_structures;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.types.Attributes;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlainTextKeyValue implements KmipStruct {

    @JsonProperty(value = "KeyMaterial")
    private KeyMaterial keyMaterial;

    @JsonProperty(value = "Attributes")
    private Optional<Attributes> attributes;

    public PlainTextKeyValue() {}

    public PlainTextKeyValue(KeyMaterial keyMaterial, Optional<Attributes> attributes) {
        this.keyMaterial = keyMaterial;
        this.attributes = attributes;
    }

    public KeyMaterial getKeyMaterial() {
        return this.keyMaterial;
    }

    public void setKeyMaterial(KeyMaterial keyMaterial) {
        this.keyMaterial = keyMaterial;
    }

    public Optional<Attributes> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Optional<Attributes> attributes) {
        this.attributes = attributes;
    }

    public PlainTextKeyValue keyMaterial(KeyMaterial keyMaterial) {
        setKeyMaterial(keyMaterial);
        return this;
    }

    public PlainTextKeyValue attributes(Optional<Attributes> attributes) {
        setAttributes(attributes);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PlainTextKeyValue)) {
            return false;
        }
        PlainTextKeyValue plainTextKeyValue = (PlainTextKeyValue)o;
        return Objects.equals(keyMaterial, plainTextKeyValue.keyMaterial)
            && Objects.equals(attributes, plainTextKeyValue.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyMaterial, attributes);
    }

    @Override
    public String toString() {
        return "{" + " keyMaterial='" + getKeyMaterial() + "'" + ", attributes='" + getAttributes() + "'" + "}";
    }

}
