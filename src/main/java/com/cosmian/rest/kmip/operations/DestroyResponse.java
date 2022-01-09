package com.cosmian.rest.kmip.operations;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class DestroyResponse implements KmipStruct {

    /**
     * The Unique Identifier of the object destroyed
     */
    @JsonProperty(value = "UniqueIdentifier")
    private String uniqueIdentifier;

    public DestroyResponse() {
    }

    public DestroyResponse(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public DestroyResponse uniqueIdentifier(String uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DestroyResponse)) {
            return false;
        }
        DestroyResponse importResponse = (DestroyResponse) o;
        return Objects.equals(uniqueIdentifier, importResponse.uniqueIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueIdentifier);
    }

    @Override
    public String toString() {
        return "{" + " uniqueIdentifier='" + getUniqueIdentifier() + "'" + "}";
    }

}
