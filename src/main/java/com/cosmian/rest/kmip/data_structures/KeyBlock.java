package com.cosmian.rest.kmip.data_structures;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.CryptographicAlgorithm;
import com.cosmian.rest.kmip.types.KeyCompressionType;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Key Block object is a structure used to encapsulate all of the information
 * that is closely associated with a cryptographic key.
 */
public class KeyBlock implements KmipStruct {

    @JsonProperty(value = "KeyFormatType")
    private KeyFormatType keyFormatType;

    @JsonProperty(value = "KeyCompressionType")
    private Optional<KeyCompressionType> keyCompressionType;

    @JsonProperty(value = "KeyValue")
    private KeyValue keyValue;

    @JsonProperty(value = "CryptographicAlgorithm")
    private CryptographicAlgorithm cryptographicAlgorithm;

    @JsonProperty(value = "CryptographicLength")
    private int cryptographicLength;

    @JsonProperty(value = "KeyWrappingData")
    private Optional<KeyWrappingData> keyWrappingData;

    /**
     * Return {@link KeyBlock} {@link Attributes} or a set of empty
     * {@link Attributes} for the given {@link ObjectType}
     */
    public Attributes attributes(ObjectType objectType) {
        Object kv = this.keyValue.get();
        if (kv instanceof PlainTextKeyValue) {
            Optional<Attributes> oa = ((PlainTextKeyValue) kv).getAttributes();
            if (oa.isPresent()) {
                return oa.get();
            }
        }
        return new Attributes(objectType, Optional.empty());
    }

    public KeyBlock() {
    }

    public KeyBlock(KeyFormatType keyFormatType, Optional<KeyCompressionType> keyCompressionType, KeyValue keyValue,
            CryptographicAlgorithm cryptographicAlgorithm, int cryptographicLength,
            Optional<KeyWrappingData> keyWrappingData) {
        this.keyFormatType = keyFormatType;
        this.keyCompressionType = keyCompressionType;
        this.keyValue = keyValue;
        this.cryptographicAlgorithm = cryptographicAlgorithm;
        this.cryptographicLength = cryptographicLength;
        this.keyWrappingData = keyWrappingData;
    }

    public KeyFormatType getKeyFormatType() {
        return this.keyFormatType;
    }

    public void setKeyFormatType(KeyFormatType keyFormatType) {
        this.keyFormatType = keyFormatType;
    }

    public Optional<KeyCompressionType> getKeyCompressionType() {
        return this.keyCompressionType;
    }

    public void setKeyCompressionType(Optional<KeyCompressionType> keyCompressionType) {
        this.keyCompressionType = keyCompressionType;
    }

    public KeyValue getKeyValue() {
        return this.keyValue;
    }

    public void setKeyValue(KeyValue keyValue) {
        this.keyValue = keyValue;
    }

    public CryptographicAlgorithm getCryptographicAlgorithm() {
        return this.cryptographicAlgorithm;
    }

    public void setCryptographicAlgorithm(CryptographicAlgorithm cryptographicAlgorithm) {
        this.cryptographicAlgorithm = cryptographicAlgorithm;
    }

    public int getCryptographicLength() {
        return this.cryptographicLength;
    }

    public void setCryptographicLength(int cryptographicLength) {
        this.cryptographicLength = cryptographicLength;
    }

    public Optional<KeyWrappingData> getKeyWrappingData() {
        return this.keyWrappingData;
    }

    public void setKeyWrappingData(Optional<KeyWrappingData> keyWrappingData) {
        this.keyWrappingData = keyWrappingData;
    }

    public KeyBlock keyFormatType(KeyFormatType keyFormatType) {
        setKeyFormatType(keyFormatType);
        return this;
    }

    public KeyBlock keyCompressionType(Optional<KeyCompressionType> keyCompressionType) {
        setKeyCompressionType(keyCompressionType);
        return this;
    }

    public KeyBlock keyValue(KeyValue keyValue) {
        setKeyValue(keyValue);
        return this;
    }

    public KeyBlock cryptographicAlgorithm(CryptographicAlgorithm cryptographicAlgorithm) {
        setCryptographicAlgorithm(cryptographicAlgorithm);
        return this;
    }

    public KeyBlock cryptographicLength(int cryptographicLength) {
        setCryptographicLength(cryptographicLength);
        return this;
    }

    public KeyBlock keyWrappingData(Optional<KeyWrappingData> keyWrappingData) {
        setKeyWrappingData(keyWrappingData);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KeyBlock)) {
            return false;
        }
        KeyBlock keyBlock = (KeyBlock) o;
        return Objects.equals(keyFormatType, keyBlock.keyFormatType)
                && Objects.equals(keyCompressionType, keyBlock.keyCompressionType)
                && Objects.equals(keyValue, keyBlock.keyValue)
                && Objects.equals(cryptographicAlgorithm, keyBlock.cryptographicAlgorithm)
                && cryptographicLength == keyBlock.cryptographicLength
                && Objects.equals(keyWrappingData, keyBlock.keyWrappingData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyFormatType, keyCompressionType, keyValue, cryptographicAlgorithm, cryptographicLength,
                keyWrappingData);
    }

    @Override
    public String toString() {
        return "{" + " keyFormatType='" + getKeyFormatType() + "'" + ", keyCompressionType='" + getKeyCompressionType()
                + "'" + ", keyValue='" + getKeyValue() + "'" + ", cryptographicAlgorithm='"
                + getCryptographicAlgorithm() + "'" + ", cryptographicLength='" + getCryptographicLength() + "'"
                + ", keyWrappingData='" + getKeyWrappingData() + "'" + "}";
    }

}
