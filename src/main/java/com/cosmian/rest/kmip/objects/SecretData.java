package com.cosmian.rest.kmip.objects;

import java.util.Objects;

import com.cosmian.CloudproofException;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.SecretDataType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecretData extends KmipObject {

    @JsonProperty(value = "SecretDataType")
    private SecretDataType secretDataType;

    @JsonProperty(value = "KeyBlock")
    private KeyBlock keyBlock;

    public SecretData() {
    }

    public SecretData(SecretDataType secretDataType, KeyBlock keyBlock) {
        this.secretDataType = secretDataType;
        this.keyBlock = keyBlock;
    }

    public SecretDataType getSecretDataType() {
        return this.secretDataType;
    }

    public void setSecretDataType(SecretDataType secretDataType) {
        this.secretDataType = secretDataType;
    }

    public KeyBlock getKeyBlock() {
        return this.keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public SecretData secretDataType(SecretDataType secretDataType) {
        setSecretDataType(secretDataType);
        return this;
    }

    public SecretData keyBlock(KeyBlock keyBlock) {
        setKeyBlock(keyBlock);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SecretData)) {
            return false;
        }
        SecretData secretData = (SecretData) o;
        return Objects.equals(secretDataType, secretData.secretDataType)
            && Objects.equals(keyBlock, secretData.keyBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secretDataType, keyBlock);
    }

    @Override
    public String toString() {
        return "{" + " secretDataType='" + getSecretDataType() + "'" + ", keyBlock='" + getKeyBlock() + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.Secret_Data;
    }

    /**
     * Deserialize an instance from its Json representation obtained using toJson()
     * 
     * @param json secret data in JSON form
     * @return the {@link SecretData}
     * @throws CloudproofException if the the JSON cannot be parsed
     */
    public static SecretData fromJson(String json) throws CloudproofException {
        return KmipObject.fromJson(json, SecretData.class);
    }
}
