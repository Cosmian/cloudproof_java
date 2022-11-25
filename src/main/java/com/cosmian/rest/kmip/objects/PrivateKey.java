package com.cosmian.rest.kmip.objects;

import java.util.Objects;

import com.cosmian.CloudproofException;
import com.cosmian.rest.kmip.KmipUtils;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Managed Cryptographic Object that is the private portion of an asymmetric key pair.
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
     * The low level cryptographic content of the Private Key
     * 
     * @return the byte array
     * @throws CloudproofException if the key is wrapped and bytes cannot be extracted
     */
    public byte[] bytes() throws CloudproofException {
        return KmipUtils.bytesFromKeyBlock(this.getKeyBlock());
    }

    /**
     * Deserialize an instance from its Json representation obtained using toJson()
     * 
     * @param json the JSON string
     * @return the {@link PrivateKey}
     * @throws CloudproofException if the parsing fails
     */
    public static PrivateKey fromJson(String json) throws CloudproofException {
        return KmipObject.fromJson(json, PrivateKey.class);
    }

    /**
     * Return the {@link Attributes} or a set of empty
     * 
     * @return the {@link Attributes}
     */
    public Attributes attributes() {
        return this.keyBlock.attributes(ObjectType.Private_Key);
    }

}
