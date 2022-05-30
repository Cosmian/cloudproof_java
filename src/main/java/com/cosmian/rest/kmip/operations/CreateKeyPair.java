package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.Attributes;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests the server to generate a new public/private key pair and register the two corresponding new
 * Managed Cryptographic Objects. The request contains attributes to be assigned to the objects (e.g., Cryptographic
 * Algorithm, Cryptographic Length, etc.). Attributes MAY be specified for both keys at the same time by specifying a
 * Common Attributes object in the request. Attributes not common to both keys (e.g., Name, Cryptographic Usage Mask)
 * MAY be specified using the Private Key Attributes and Public Key Attributes objects in the request, which take
 * precedence over the Common Attributes object. For the Private Key, the server SHALL create a Link attribute of Link
 * Type Public Key pointing to the Public Key. For the Public Key, the server SHALL create a Link attribute of Link Type
 * Private Key pointing to the Private Key. The response contains the Unique Identifiers of both created objects. The ID
 * Placeholder value SHALL be set to the Unique Identifier of the Private Key.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class CreateKeyPair implements KmipStruct {

    /**
     * Specifies desired attributes to be associated with the new object that apply to both the Private and Public Key
     * Objects
     */
    @JsonProperty("CommonAttributes")
    private Optional<Attributes> commonAttributes = Optional.empty();

    /**
     * Specifies the attributes to be associated with the new object that apply to the Private Key Object.
     */
    @JsonProperty("PrivateKeyAttributes")
    private Optional<Attributes> privateKeyAttributes = Optional.empty();

    /**
     * Specifies the attributes to be associated with the new object that apply to the Public Key Object.
     */
    @JsonProperty("PublicKeyAttributes")
    private Optional<Attributes> publicKeyAttributes = Optional.empty();

    /**
     * Specifies all ProtectionStorage Mask selections that are permissible for the new Private Key and Public Key
     * objects.
     */
    @JsonProperty(value = "CommonProtectionStorageMasks")
    private Optional<Integer> commonProtectionStorageMasks = Optional.empty();

    /**
     * Specifies all ProtectionStorage Mask selections that are permissible for the new Private Key object.
     */
    @JsonProperty(value = "PrivateProtectionStorageMasks")
    private Optional<Integer> privateProtectionStorageMasks = Optional.empty();

    /**
     * Specifies all ProtectionStorage Mask selections that are permissible for the new PublicKey object.
     */
    @JsonProperty(value = "PublicProtectionStorageMasks")
    private Optional<Integer> publicProtectionStorageMasks = Optional.empty();

    public CreateKeyPair() {
    }

    public CreateKeyPair(Optional<Attributes> commonAttributes, Optional<Attributes> privateKeyAttributes,
        Optional<Attributes> publicKeyAttributes, Optional<Integer> commonProtectionStorageMasks,
        Optional<Integer> privateProtectionStorageMasks, Optional<Integer> publicProtectionStorageMasks) {
        this.commonAttributes = commonAttributes;
        this.privateKeyAttributes = privateKeyAttributes;
        this.publicKeyAttributes = publicKeyAttributes;
        this.commonProtectionStorageMasks = commonProtectionStorageMasks;
        this.privateProtectionStorageMasks = privateProtectionStorageMasks;
        this.publicProtectionStorageMasks = publicProtectionStorageMasks;
    }

    public CreateKeyPair(Optional<Attributes> commonAttributes, Optional<Integer> commonProtectionStorageMasks) {
        this.commonAttributes = commonAttributes;
        this.commonProtectionStorageMasks = commonProtectionStorageMasks;
    }

    public Optional<Attributes> getCommonAttributes() {
        return this.commonAttributes;
    }

    public void setCommonAttributes(Optional<Attributes> commonAttributes) {
        this.commonAttributes = commonAttributes;
    }

    public Optional<Attributes> getPrivateKeyAttributes() {
        return this.privateKeyAttributes;
    }

    public void setPrivateKeyAttributes(Optional<Attributes> privateKeyAttributes) {
        this.privateKeyAttributes = privateKeyAttributes;
    }

    public Optional<Attributes> getPublicKeyAttributes() {
        return this.publicKeyAttributes;
    }

    public void setPublicKeyAttributes(Optional<Attributes> publicKeyAttributes) {
        this.publicKeyAttributes = publicKeyAttributes;
    }

    public Optional<Integer> getCommonProtectionStorageMasks() {
        return this.commonProtectionStorageMasks;
    }

    public void setCommonProtectionStorageMasks(Optional<Integer> commonProtectionStorageMasks) {
        this.commonProtectionStorageMasks = commonProtectionStorageMasks;
    }

    public Optional<Integer> getPrivateProtectionStorageMasks() {
        return this.privateProtectionStorageMasks;
    }

    public void setPrivateProtectionStorageMasks(Optional<Integer> privateProtectionStorageMasks) {
        this.privateProtectionStorageMasks = privateProtectionStorageMasks;
    }

    public Optional<Integer> getPublicProtectionStorageMasks() {
        return this.publicProtectionStorageMasks;
    }

    public void setPublicProtectionStorageMasks(Optional<Integer> publicProtectionStorageMasks) {
        this.publicProtectionStorageMasks = publicProtectionStorageMasks;
    }

    public CreateKeyPair commonAttributes(Optional<Attributes> commonAttributes) {
        setCommonAttributes(commonAttributes);
        return this;
    }

    public CreateKeyPair privateKeyAttributes(Optional<Attributes> privateKeyAttributes) {
        setPrivateKeyAttributes(privateKeyAttributes);
        return this;
    }

    public CreateKeyPair publicKeyAttributes(Optional<Attributes> publicKeyAttributes) {
        setPublicKeyAttributes(publicKeyAttributes);
        return this;
    }

    public CreateKeyPair commonProtectionStorageMasks(Optional<Integer> commonProtectionStorageMasks) {
        setCommonProtectionStorageMasks(commonProtectionStorageMasks);
        return this;
    }

    public CreateKeyPair privateProtectionStorageMasks(Optional<Integer> privateProtectionStorageMasks) {
        setPrivateProtectionStorageMasks(privateProtectionStorageMasks);
        return this;
    }

    public CreateKeyPair publicProtectionStorageMasks(Optional<Integer> publicProtectionStorageMasks) {
        setPublicProtectionStorageMasks(publicProtectionStorageMasks);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CreateKeyPair)) {
            return false;
        }
        CreateKeyPair createKeyPair = (CreateKeyPair) o;
        return Objects.equals(commonAttributes, createKeyPair.commonAttributes)
            && Objects.equals(privateKeyAttributes, createKeyPair.privateKeyAttributes)
            && Objects.equals(publicKeyAttributes, createKeyPair.publicKeyAttributes)
            && Objects.equals(commonProtectionStorageMasks, createKeyPair.commonProtectionStorageMasks)
            && Objects.equals(privateProtectionStorageMasks, createKeyPair.privateProtectionStorageMasks)
            && Objects.equals(publicProtectionStorageMasks, createKeyPair.publicProtectionStorageMasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commonAttributes, privateKeyAttributes, publicKeyAttributes, commonProtectionStorageMasks,
            privateProtectionStorageMasks, publicProtectionStorageMasks);
    }

    @Override
    public String toString() {
        return "{" + " commonAttributes='" + getCommonAttributes() + "'" + ", privateKeyAttributes='"
            + getPrivateKeyAttributes() + "'" + ", publicKeyAttributes='" + getPublicKeyAttributes() + "'"
            + ", commonProtectionStorageMasks='" + getCommonProtectionStorageMasks() + "'"
            + ", privateProtectionStorageMasks='" + getPrivateProtectionStorageMasks() + "'"
            + ", publicProtectionStorageMasks='" + getPublicProtectionStorageMasks() + "'" + "}";
    }

}
