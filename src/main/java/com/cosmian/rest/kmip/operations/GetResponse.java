package com.cosmian.rest.kmip.operations;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.objects.KmipObject;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class GetResponse implements KmipStruct {

    // Determines the type of object being retrieved.
    @JsonProperty(value = "ObjectType")
    private ObjectType objectType;

    // The Unique Identifier of the object to be retrieved
    @JsonProperty(value = "UniqueIdentifier")
    private String uniqueIdentifier;

    // The object being retrieved.
    @JsonProperty(value = "Object")
    private KmipObject object;

    public GetResponse() {
    }

    public GetResponse(ObjectType objectType, String uniqueIdentifier, KmipObject object) {
        this.objectType = objectType;
        this.uniqueIdentifier = uniqueIdentifier;
        this.object = object;
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

    public KmipObject getObject() {
        return this.object;
    }

    public void setObject(KmipObject object) {
        this.object = object;
    }

    public GetResponse objectType(ObjectType objectType) {
        setObjectType(objectType);
        return this;
    }

    public GetResponse uniqueIdentifier(String uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    public GetResponse object(KmipObject object) {
        setObject(object);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GetResponse)) {
            return false;
        }
        GetResponse getResponse = (GetResponse) o;
        return Objects.equals(objectType, getResponse.objectType)
            && Objects.equals(uniqueIdentifier, getResponse.uniqueIdentifier)
            && Objects.equals(object, getResponse.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, uniqueIdentifier, object);
    }

    @Override
    public String toString() {
        return "{" + " objectType='" + getObjectType() + "'" + ", uniqueIdentifier='" + getUniqueIdentifier() + "'"
            + ", object='" + getObject() + "'" + "}";
    }

}
