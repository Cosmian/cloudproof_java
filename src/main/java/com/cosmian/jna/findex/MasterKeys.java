package com.cosmian.jna.findex;

import com.cosmian.CloudproofException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MasterKeys {
    @JsonProperty("k")
    private byte[] k;

    @JsonProperty("k_star")
    private byte[] k_star;

    public MasterKeys() {
    }

    public MasterKeys(byte[] k, byte[] k_star) {
        this.k = k;
        this.k_star = k_star;
    }

    public byte[] getK() {
        return k;
    }

    public byte[] getK_star() {
        return k_star;
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
            throw new CloudproofException("Failed serializing to JSON the MasterKeys.class: " + e.getMessage(), e);
        }
    }

    public static MasterKeys fromJson(String json) throws CloudproofException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, MasterKeys.class);
        } catch (JsonProcessingException e) {
            throw new CloudproofException(
                "Failed deserializing from JSON the MasterKeys.class " + ": " + e.getMessage(),
                e);
        }
    }

}
