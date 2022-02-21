package com.cosmian.rest.kmip.objects;

import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.KmipUtils;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicKey extends KmipObject {

    @JsonProperty(value = "KeyBlock")
    private KeyBlock keyBlock;

    public PublicKey() {
    }

    public PublicKey(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public KeyBlock getKeyBlock() {
        return this.keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public PublicKey keyBlock(KeyBlock keyBlock) {
        setKeyBlock(keyBlock);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PublicKey)) {
            return false;
        }
        PublicKey publicKey = (PublicKey) o;
        return Objects.equals(keyBlock, publicKey.keyBlock);
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
        return ObjectType.Public_Key;
    }

    /**
     * Return the {@link Attributes} or a set of empty
     * {@link Attributes}
     * 
     * @return the {@link Attributes} found in the {@link KeyBlock}
     */
    public Attributes attributes() {
        return this.keyBlock.attributes(ObjectType.Private_Key);
    }

    /**
     * The low level cryptographic content of the Public Key
     * 
     * @return the byte array
     * @throws CosmianException if the key is wrapped and bytes cannot be extracted
     */
    public byte[] bytes() throws CosmianException {
        return KmipUtils.bytesFromKeyBlock(this.getKeyBlock());
    }

    /**
     * 
     * Deserialize an instance from its Json representation obtained using
     * oJson()
     * 
     * @param json the public key in JSON form
     * @return the public key
     * @throws CosmianException if the parsing fails
     */
    public static PublicKey fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, PublicKey.class);
    }

}
