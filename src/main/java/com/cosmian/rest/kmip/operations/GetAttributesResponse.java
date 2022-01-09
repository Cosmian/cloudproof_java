package com.cosmian.rest.kmip.operations;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.Attributes;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class GetAttributesResponse implements KmipStruct {

    // The Unique Identifier of the object
    @JsonProperty(value = "UniqueIdentifier")
    private String uniqueIdentifier;

    // Attributes
    @JsonProperty(value = "Attributes")
    private Attributes attributes;

    public GetAttributesResponse() {
    }

    public GetAttributesResponse(String uniqueIdentifier, Attributes attributes) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.attributes = attributes;
    }

    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Attributes getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public GetAttributesResponse uniqueIdentifier(String uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    public GetAttributesResponse attributes(Attributes attributes) {
        setAttributes(attributes);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GetAttributesResponse)) {
            return false;
        }
        GetAttributesResponse getAttributesResponse = (GetAttributesResponse) o;
        return Objects.equals(uniqueIdentifier, getAttributesResponse.uniqueIdentifier)
                && Objects.equals(attributes, getAttributesResponse.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueIdentifier, attributes);
    }

    @Override
    public String toString() {
        return "{" + " uniqueIdentifier='" + getUniqueIdentifier() + "'" + ", attributes='" + getAttributes() + "'"
                + "}";
    }

}
