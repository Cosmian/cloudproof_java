package com.cosmian.rest.kmip.objects;

import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Managed Cryptographic Object that is a text-based representation of a PGP key. The Key Block field, indicated
 * below, will contain the ASCII-armored export of a PGP key in the format as specified in RFC 4880. It MAY contain only
 * a public key block, or both a public and private key block. Two different versions of PGP keys, version 3 and version
 * 4, MAY be stored in this Managed Cryptographic Object. KMIP implementers SHOULD treat the Key Block field as an
 * opaque blob. PGP-aware KMIP clients SHOULD take on the responsibility of decomposing the Key Block into other Managed
 * Cryptographic Objects (Public Keys, Private Keys, etc.).
 */
public class PGPKey extends KmipObject {

    @JsonProperty(value = "PGPKeyVersion")
    private int pgp_key_version;

    @JsonProperty(value = "KeyBlock")
    private KeyBlock keyBlock;

    public PGPKey() {
    }

    public PGPKey(int pgp_key_version, KeyBlock keyBlock) {
        this.pgp_key_version = pgp_key_version;
        this.keyBlock = keyBlock;
    }

    public int getPgp_key_version() {
        return this.pgp_key_version;
    }

    public void setPgp_key_version(int pgp_key_version) {
        this.pgp_key_version = pgp_key_version;
    }

    public KeyBlock getKeyBlock() {
        return this.keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public PGPKey pgp_key_version(int pgp_key_version) {
        setPgp_key_version(pgp_key_version);
        return this;
    }

    public PGPKey keyBlock(KeyBlock keyBlock) {
        setKeyBlock(keyBlock);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PGPKey)) {
            return false;
        }
        PGPKey pGPKey = (PGPKey) o;
        return pgp_key_version == pGPKey.pgp_key_version && Objects.equals(keyBlock, pGPKey.keyBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pgp_key_version, keyBlock);
    }

    @Override
    public String toString() {
        return "{" + " pgp_key_version='" + getPgp_key_version() + "'" + ", keyBlock='" + getKeyBlock() + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.PGP_Key;
    }

    /**
     * Deserialize an instance from its Json representation obtained using toJson()
     * 
     * @param json the JSON string
     * @return the {@link PGPKey}
     * @throws CosmianException if the parsing fails
     */
    public static PGPKey fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, PGPKey.class);
    }
}
