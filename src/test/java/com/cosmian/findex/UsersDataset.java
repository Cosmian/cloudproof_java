package com.cosmian.findex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UsersDataset {
    @JsonProperty("id")
    public long id;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;

    @JsonProperty("phone")
    public String phone;

    @JsonProperty("email")
    public String email;

    @JsonProperty("country")
    public String country;

    @JsonProperty("region")
    public String region;

    @JsonProperty("employeeNumber")
    public String employeeNumber;

    @JsonProperty("security")
    public String security;

    public UsersDataset() {
    }

    public Set<Keyword> values() {
        return new HashSet<>(
            Arrays.asList(new Keyword(this.firstName), new Keyword(this.lastName), new Keyword(this.phone),
                new Keyword(this.email), new Keyword(this.country), new Keyword(this.region),
                new Keyword(this.employeeNumber),
                new Keyword(this.security)));
    }

    public String toString() {
        return this.firstName + this.lastName + this.phone + this.email + this.country + this.region
            + this.employeeNumber + this.security;
    }

    /**
     * This method is mostly used for local tests and serialization.
     *
     * @return the JSON string
     * @throws CloudproofException if the serialization fails
     */
    public String toJson() throws CloudproofException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Failed serializing to JSON the TestFindexDataset.class: " + e.getMessage(),
                e);
        }
    }

    public static UsersDataset[] fromJson(String json) throws CloudproofException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, UsersDataset[].class);
        } catch (JsonProcessingException e) {
            throw new CloudproofException(
                "Failed deserializing from JSON the TestFindexDataset.class " + ": " + e.getMessage(), e);
        }
    }

}
