package com.cosmian.rest.kmip.operations;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class ReKeyKeyPairResponse implements KmipStruct {

    /**
     * The Unique Identifier of the newly created replacement Private Key object.
     */
    @JsonProperty(value = "PrivateKeyUniqueIdentifier")
    private String privateKeyUniqueIdentifier;

    /**
     * The Unique Identifier of the newly created replacement Public Key object.
     */
    @JsonProperty(value = "PublicKeyUniqueIdentifier")
    private String publicKeyUniqueIdentifier;

    public ReKeyKeyPairResponse() {
    }

    public ReKeyKeyPairResponse(String privateKeyUniqueIdentifier, String publicKeyUniqueIdentifier) {
        this.privateKeyUniqueIdentifier = privateKeyUniqueIdentifier;
        this.publicKeyUniqueIdentifier = publicKeyUniqueIdentifier;
    }

    public String getPrivateKeyUniqueIdentifier() {
        return this.privateKeyUniqueIdentifier;
    }

    public void setPrivateKeyUniqueIdentifier(String privateKeyUniqueIdentifier) {
        this.privateKeyUniqueIdentifier = privateKeyUniqueIdentifier;
    }

    public String getPublicKeyUniqueIdentifier() {
        return this.publicKeyUniqueIdentifier;
    }

    public void setPublicKeyUniqueIdentifier(String publicKeyUniqueIdentifier) {
        this.publicKeyUniqueIdentifier = publicKeyUniqueIdentifier;
    }

    public ReKeyKeyPairResponse privateKeyUniqueIdentifier(String privateKeyUniqueIdentifier) {
        setPrivateKeyUniqueIdentifier(privateKeyUniqueIdentifier);
        return this;
    }

    public ReKeyKeyPairResponse publicKeyUniqueIdentifier(String publicKeyUniqueIdentifier) {
        setPublicKeyUniqueIdentifier(publicKeyUniqueIdentifier);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ReKeyKeyPairResponse)) {
            return false;
        }
        ReKeyKeyPairResponse reKeyKeypairResponse = (ReKeyKeyPairResponse) o;
        return Objects.equals(privateKeyUniqueIdentifier, reKeyKeypairResponse.privateKeyUniqueIdentifier)
                && Objects.equals(publicKeyUniqueIdentifier, reKeyKeypairResponse.publicKeyUniqueIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKeyUniqueIdentifier, publicKeyUniqueIdentifier);
    }

    @Override
    public String toString() {
        return "{" +
                " privateKeyUniqueIdentifier='" + getPrivateKeyUniqueIdentifier() + "'" +
                ", publicKeyUniqueIdentifier='" + getPublicKeyUniqueIdentifier() + "'" +
                "}";
    }

}
