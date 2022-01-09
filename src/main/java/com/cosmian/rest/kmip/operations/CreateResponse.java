package com.cosmian.rest.kmip.operations;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class CreateResponse implements KmipStruct {

    /// ype of object created.
    @JsonProperty(value = "ObjectType")
    private ObjectType objectType;

    /// The Unique Identifier of the object created
    @JsonProperty(value = "UniqueIdentifier")
    private String uniqueIdentifier;

    public CreateResponse() {
    }

    public CreateResponse(ObjectType objectType, String uniqueIdentifier) {
        this.objectType = objectType;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public CreateResponse objectType(ObjectType objectType) {
        setObjectType(objectType);
        return this;
    }

    public CreateResponse uniqueIdentifier(String uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CreateResponse)) {
            return false;
        }
        CreateResponse createResponse = (CreateResponse) o;
        return Objects.equals(objectType, createResponse.objectType)
                && Objects.equals(uniqueIdentifier, createResponse.uniqueIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, uniqueIdentifier);
    }

    @Override
    public String toString() {
        return "{" + " objectType='" + getObjectType() + "'" + ", uniqueIdentifier='" + getUniqueIdentifier() + "'"
                + "}";
    }

}
