package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.AttributeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests one or more attributes associated with a Managed
 * Object. The object is specified by its Unique Identifier, and the attributes
 * are specified by their name in the request. If a specified attribute has
 * multiple instances, then all instances are returned. If a specified attribute
 * does not exist (i.e., has no value), then it SHALL NOT be present in the
 * returned response. If none of the requested attributes exist, then the
 * response SHALL consist only of the Unique Identifier. The same Attribute
 * Reference SHALL NOT be present more than once in a request.
 * 
 * If no Attribute Reference is provided, the server SHALL return all
 * attributes.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class GetAttributes implements KmipStruct {

    /**
     * Determines the object whose attributes
     * are being requested. If omitted, then
     * the ID Placeholder value is used by the
     * server as the Unique Identifier.
     */
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> unique_identifier;

    /**
     * Specifies an attribute associated with
     * the object.
     */
    @JsonProperty(value = "AttributeReference")
    private Optional<AttributeReference[]> attribute_references;

    public GetAttributes() {
    }

    public GetAttributes(Optional<String> unique_identifier, Optional<AttributeReference[]> attribute_references) {
        this.unique_identifier = unique_identifier;
        this.attribute_references = attribute_references;
    }

    public Optional<String> getUnique_identifier() {
        return this.unique_identifier;
    }

    public void setUnique_identifier(Optional<String> unique_identifier) {
        this.unique_identifier = unique_identifier;
    }

    public Optional<AttributeReference[]> getAttribute_references() {
        return this.attribute_references;
    }

    public void setAttribute_references(Optional<AttributeReference[]> attribute_references) {
        this.attribute_references = attribute_references;
    }

    public GetAttributes unique_identifier(Optional<String> unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    public GetAttributes attribute_references(Optional<AttributeReference[]> attribute_references) {
        setAttribute_references(attribute_references);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GetAttributes)) {
            return false;
        }
        GetAttributes getAttributes = (GetAttributes) o;
        return Objects.equals(unique_identifier, getAttributes.unique_identifier)
                && Objects.equals(attribute_references, getAttributes.attribute_references);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unique_identifier, attribute_references);
    }

    @Override
    public String toString() {
        return "{" + " unique_identifier='" + getUnique_identifier() + "'" + ", attribute_references='"
                + getAttribute_references() + "'" + "}";
    }

}
