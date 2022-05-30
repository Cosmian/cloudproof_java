package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class LocateResponse implements KmipStruct {

    /**
     * An Integer object that indicates the number of object identifiers that satisfy the identification criteria
     * specified in the request. A server MAY elect to omit this value from the Response if it is unable or unwilling to
     * determine the total count of matched items. A server MAY elect to return the Located Items value even if Offset
     * Items is not present in the Request.
     */
    @JsonProperty(value = "LocatedItems")
    private Optional<Integer> located_items;

    /**
     * The Unique Identifier of the located objects.
     */
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String[]> unique_identifier;

    public LocateResponse() {
    }

    public LocateResponse(Optional<Integer> located_items, Optional<String[]> unique_identifier) {
        this.located_items = located_items;
        this.unique_identifier = unique_identifier;
    }

    public Optional<Integer> getLocated_items() {
        return this.located_items;
    }

    public void setLocated_items(Optional<Integer> located_items) {
        this.located_items = located_items;
    }

    public Optional<String[]> getUnique_identifier() {
        return this.unique_identifier;
    }

    public void setUnique_identifier(Optional<String[]> unique_identifier) {
        this.unique_identifier = unique_identifier;
    }

    public LocateResponse located_items(Optional<Integer> located_items) {
        setLocated_items(located_items);
        return this;
    }

    public LocateResponse unique_identifier(Optional<String[]> unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LocateResponse)) {
            return false;
        }
        LocateResponse locateResponse = (LocateResponse) o;
        return Objects.equals(located_items, locateResponse.located_items)
            && Objects.equals(unique_identifier, locateResponse.unique_identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(located_items, unique_identifier);
    }

    @Override
    public String toString() {
        return "{" + " located_items='" + getLocated_items() + "'" + ", unique_identifier='" + getUnique_identifier()
            + "'" + "}";
    }

}
