package com.cosmian.findex;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.Word;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UsersDataset {
    @JsonProperty("id")
    public int id;

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

    public Word[] values() {
        return new Word[] {new Word(this.firstName), new Word(this.lastName), new Word(this.phone),
            new Word(this.email), new Word(this.country), new Word(this.region), new Word(this.employeeNumber),
            new Word(this.security)};
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
