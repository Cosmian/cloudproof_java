package com.cosmian.rest.kmip.operations;

import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.Link;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class TestStruct implements KmipStruct {

    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> unique_identifier;

    @JsonProperty(value = "Link")
    private Link[] link;

    public TestStruct() {
    }

    public TestStruct(Optional<String> unique_identifier, Link[] link) {
        this.unique_identifier = unique_identifier;
        this.link = link;
    }

    public Optional<String> getUnique_identifier() {
        return this.unique_identifier;
    }

    public void setUnique_identifier(Optional<String> unique_identifier) {
        this.unique_identifier = unique_identifier;
    }

    public Link[] getLink() {
        return this.link;
    }

    public void setLink(Link[] link) {
        this.link = link;
    }

}
