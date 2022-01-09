package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation is used to indicate to the server that the key material for
 * the specified Managed Object SHALL be destroyed or rendered inaccessible. The
 * meta-data for the key material SHALL be retained by the server. Objects SHALL
 * only be destroyed if they are in either Pre-Active or Deactivated state.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Destroy implements KmipStruct {

    /**
     * Determines the object being destroyed. If omitted, then the ID
     * Placeholder value is used by the server as the Unique Identifier.
     */
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> uniqueIdentifier = Optional.empty();

    public Destroy() {
    }

    public Destroy(Optional<String> uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Optional<String> getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(Optional<String> uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Destroy uniqueIdentifier(Optional<String> uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Destroy)) {
            return false;
        }
        Destroy destroy = (Destroy) o;
        return Objects.equals(uniqueIdentifier, destroy.uniqueIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueIdentifier);
    }

    @Override
    public String toString() {
        return "{" +
                " uniqueIdentifier='" + getUniqueIdentifier() + "'" +
                "}";
    }

}
