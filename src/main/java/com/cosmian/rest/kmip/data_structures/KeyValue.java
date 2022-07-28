package com.cosmian.rest.kmip.data_structures;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.types.Attributes;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Key Value is used only inside a Key Block and is either a Byte String or a: • The Key Value structure contains
 * the key material, either as a byte string or as a Transparent Key structure, and OPTIONAL attribute information that
 * is associated and encapsulated with the key material. This attribute information differs from the attributes
 * associated with Managed Objects, and is obtained via the Get Attributes operation, only by the fact that it is
 * encapsulated with (and possibly wrapped with) the key material itself. • The Key Value Byte String is either the
 * wrapped TTLV-encoded Key Value structure, or the wrapped un-encoded value of the Byte String Key Material field.
 */
public class KeyValue implements KmipStruct {

    @JsonProperty(value = "KeyMaterial")
    private KeyMaterial keyMaterial;

    @JsonProperty(value = "Attributes")
    private Optional<Attributes> attributes;

    public KeyValue() {
    }

    public KeyValue(KeyMaterial keyMaterial, Optional<Attributes> attributes) {
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

    public KeyValue keyMaterial(KeyMaterial keyMaterial) {
        setKeyMaterial(keyMaterial);
        return this;
    }

    public KeyValue attributes(Optional<Attributes> attributes) {
        setAttributes(attributes);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KeyValue)) {
            return false;
        }
        KeyValue keyValue = (KeyValue) o;
        return Objects.equals(keyMaterial, keyValue.keyMaterial) && Objects.equals(attributes, keyValue.attributes);
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
