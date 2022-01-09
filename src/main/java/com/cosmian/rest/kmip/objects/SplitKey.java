package com.cosmian.rest.kmip.objects;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.SplitKeyMethod;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SplitKey extends KmipObject {

    @JsonProperty("SplitKeyParts")
    private int split_key_parts;

    @JsonProperty("KeyPartIdentifier")
    private int key_part_identifier;

    @JsonProperty("SplitKeyThreshold")
    private int split_key_threshold;

    @JsonProperty("SplitKeyMethod")
    private SplitKeyMethod split_key_method;

    /// REQUIRED only if Split Key Method is Polynomial Sharing Prime Field.
    @JsonProperty("PrimeFieldSize")
    private Optional<BigInteger> prime_field_size;

    @JsonProperty(value = "KeyBlock")
    private KeyBlock keyBlock;

    public SplitKey() {
    }

    public SplitKey(int split_key_parts, int key_part_identifier, int split_key_threshold,
            SplitKeyMethod split_key_method, Optional<BigInteger> prime_field_size, KeyBlock keyBlock) {
        this.split_key_parts = split_key_parts;
        this.key_part_identifier = key_part_identifier;
        this.split_key_threshold = split_key_threshold;
        this.split_key_method = split_key_method;
        this.prime_field_size = prime_field_size;
        this.keyBlock = keyBlock;
    }

    public int getSplit_key_parts() {
        return this.split_key_parts;
    }

    public void setSplit_key_parts(int split_key_parts) {
        this.split_key_parts = split_key_parts;
    }

    public int getKey_part_identifier() {
        return this.key_part_identifier;
    }

    public void setKey_part_identifier(int key_part_identifier) {
        this.key_part_identifier = key_part_identifier;
    }

    public int getSplit_key_threshold() {
        return this.split_key_threshold;
    }

    public void setSplit_key_threshold(int split_key_threshold) {
        this.split_key_threshold = split_key_threshold;
    }

    public SplitKeyMethod getSplit_key_method() {
        return this.split_key_method;
    }

    public void setSplit_key_method(SplitKeyMethod split_key_method) {
        this.split_key_method = split_key_method;
    }

    public Optional<BigInteger> getPrime_field_size() {
        return this.prime_field_size;
    }

    public void setPrime_field_size(Optional<BigInteger> prime_field_size) {
        this.prime_field_size = prime_field_size;
    }

    public KeyBlock getKeyBlock() {
        return this.keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public SplitKey split_key_parts(int split_key_parts) {
        setSplit_key_parts(split_key_parts);
        return this;
    }

    public SplitKey key_part_identifier(int key_part_identifier) {
        setKey_part_identifier(key_part_identifier);
        return this;
    }

    public SplitKey split_key_threshold(int split_key_threshold) {
        setSplit_key_threshold(split_key_threshold);
        return this;
    }

    public SplitKey split_key_method(SplitKeyMethod split_key_method) {
        setSplit_key_method(split_key_method);
        return this;
    }

    public SplitKey prime_field_size(Optional<BigInteger> prime_field_size) {
        setPrime_field_size(prime_field_size);
        return this;
    }

    public SplitKey keyBlock(KeyBlock keyBlock) {
        setKeyBlock(keyBlock);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SplitKey)) {
            return false;
        }
        SplitKey splitKey = (SplitKey) o;
        return split_key_parts == splitKey.split_key_parts && key_part_identifier == splitKey.key_part_identifier
                && split_key_threshold == splitKey.split_key_threshold
                && Objects.equals(split_key_method, splitKey.split_key_method)
                && Objects.equals(prime_field_size, splitKey.prime_field_size)
                && Objects.equals(keyBlock, splitKey.keyBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(split_key_parts, key_part_identifier, split_key_threshold, split_key_method,
                prime_field_size, keyBlock);
    }

    @Override
    public String toString() {
        return "{" + " split_key_parts='" + getSplit_key_parts() + "'" + ", key_part_identifier='"
                + getKey_part_identifier() + "'" + ", split_key_threshold='" + getSplit_key_threshold() + "'"
                + ", split_key_method='" + getSplit_key_method() + "'" + ", prime_field_size='" + getPrime_field_size()
                + "'" + ", keyBlock='" + getKeyBlock() + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.Split_Key;
    }

    /**
     * 
     * Deserialize an instance from its Json representation obtained using
     * {@link toJson()}
     */
    public static SplitKey fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, SplitKey.class);
    }
}
