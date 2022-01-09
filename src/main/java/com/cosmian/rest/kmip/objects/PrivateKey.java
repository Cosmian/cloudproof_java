package com.cosmian.rest.kmip.objects;

import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Managed Cryptographic Object that is the private portion of an asymmetric
 * key pair.
 */
public class PrivateKey extends KmipObject {

    @JsonProperty(value = "KeyBlock")
    private KeyBlock keyBlock;

    public PrivateKey() {
    }

    public PrivateKey(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public KeyBlock getKeyBlock() {
        return this.keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public PrivateKey keyBlock(KeyBlock keyBlock) {
        setKeyBlock(keyBlock);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PrivateKey)) {
            return false;
        }
        PrivateKey privateKey = (PrivateKey) o;
        return Objects.equals(keyBlock, privateKey.keyBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(keyBlock);
    }

    @Override
    public String toString() {
        return "{" + " keyBlock='" + getKeyBlock() + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.Private_Key;
    }

    /**
     * 
     * Deserialize an instance from its Json representation obtained using
     * {@link toJson()}
     */
    public static PrivateKey fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, PrivateKey.class);
    }

    /**
     * Return the {@link Attributes} or a set of empty
     * {@link Attributes}
     */
    public Attributes attributes() {
        return this.keyBlock.attributes(ObjectType.Private_Key);
    }

}
