package com.cosmian.rest.kmip.objects;

import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SymmetricKey extends KmipObject {

    @JsonProperty(value = "KeyBlock")
    private KeyBlock keyBlock;

    public SymmetricKey() {
    }

    public SymmetricKey(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public KeyBlock getKeyBlock() {
        return this.keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public SymmetricKey keyBlock(KeyBlock keyBlock) {
        setKeyBlock(keyBlock);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SymmetricKey)) {
            return false;
        }
        SymmetricKey symmetricKey = (SymmetricKey) o;
        return Objects.equals(keyBlock, symmetricKey.keyBlock);
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
        return ObjectType.Symmetric_Key;
    }

    /**
     * Return the {@link Attributes} or a set of empty
     * {@link Attributes}
     */
    public Attributes attributes() {
        return this.keyBlock.attributes(ObjectType.Private_Key);
    }

    /**
     * 
     * Deserialize an instance from its Json representation obtained using
     * {@link toJson()}
     */
    public static SymmetricKey fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, SymmetricKey.class);
    }
}
