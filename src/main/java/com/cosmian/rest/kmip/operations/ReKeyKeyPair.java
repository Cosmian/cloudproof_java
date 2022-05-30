package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ProtectionStorageMasks;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This request is used to generate a replacement key pair for an existing public/private key pair. It is analogous to
 * the Create Key Pair operation, except that attributes of the replacement key pair are copied from the existing key
 * pair, with the exception of the attributes listed in Re-key Key Pair Attribute Requirements tor. As the replacement
 * of the key pair takes over the name attribute for the existing public/private key pair, Re-key Key Pair SHOULD only
 * be performed once on a given key pair. For both the existing public key and private key, the server SHALL create a
 * Link attribute of Link Type Replacement Key pointing to the replacement public and private key, respectively. For
 * both the replacement public and private key, the server SHALL create a Link attribute of Link Type Replaced Key
 * pointing to the existing public and private key, respectively. The server SHALL copy the Private Key Unique
 * Identifier of the replacement private key returned by this operation into the ID Placeholder variable. An Offset MAY
 * be used to indicate the difference between the Initial Date and the Activation Date of the replacement key pair. If
 * no Offset is specified, the Activation Date and Deactivation Date values are copied from the existing key pair. If
 * Offset is set and dates exist for the existing key pair, then the dates of the replacement key pair SHALL be set
 * based on the dates of the existing key pair as follows
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class ReKeyKeyPair implements KmipStruct {

    /**
     * Determines the existing Asymmetric key pair to be re-keyed. If omitted, then the ID Placeholder is substituted by
     * the server.
     */
    @JsonProperty(value = "PrivateKeyUniqueIdentifier")
    private Optional<String> privateKeyUniqueIdentifier = Optional.empty();

    /**
     * An Interval object indicating the difference between the Initial Date and the Activation Date of the replacement
     * key pair to be created.
     */
    @JsonProperty(value = "Offset")
    private Optional<Integer> offset = Optional.empty();

    /**
     * Specifies desired attributes that apply to both the Private and Public Key Objects.
     */
    @JsonProperty(value = "CommonAttributes")
    private Optional<Attributes> common_attributes = Optional.empty();

    /**
     * Specifies desired attributes that apply the Private Key Objects.
     */
    @JsonProperty(value = "PrivateKeyAttributes")
    private Optional<Attributes> privateKeyAttributes = Optional.empty();

    /**
     * Specifies desired attributes that apply the Public Key Objects.
     */
    @JsonProperty(value = "PublicKeyAttributes")
    private Optional<Attributes> publicKeyAttributes = Optional.empty();

    /**
     * Specifies all Protection Storage Mask selections that are permissible for the new Private Key and new Public Key
     * objects
     */
    @JsonProperty(value = "CommonProtectionStorageMasks")
    private Optional<ProtectionStorageMasks> common_protection_storage_masks = Optional.empty();

    /**
     * Specifies all Protection Storage Mask selections that are permissible for the new Private Key objects
     */
    @JsonProperty(value = "PrivateProtectionStorageMasks")
    private Optional<ProtectionStorageMasks> private_protection_storage_masks = Optional.empty();

    /**
     * Specifies all Protection Storage Mask selections that are permissible for the new Public Key objects
     */
    @JsonProperty(value = "PublicProtectionStorageMasks")
    private Optional<ProtectionStorageMasks> public_protection_storage_masks = Optional.empty();

    public ReKeyKeyPair() {
    }

    public ReKeyKeyPair(Optional<String> privateKeyUniqueIdentifier, Optional<Integer> offset,
        Optional<Attributes> common_attributes, Optional<Attributes> privateKeyAttributes,
        Optional<Attributes> publicKeyAttributes, Optional<ProtectionStorageMasks> common_protection_storage_masks,
        Optional<ProtectionStorageMasks> private_protection_storage_masks,
        Optional<ProtectionStorageMasks> public_protection_storage_masks) {
        this.privateKeyUniqueIdentifier = privateKeyUniqueIdentifier;
        this.offset = offset;
        this.common_attributes = common_attributes;
        this.privateKeyAttributes = privateKeyAttributes;
        this.publicKeyAttributes = publicKeyAttributes;
        this.common_protection_storage_masks = common_protection_storage_masks;
        this.private_protection_storage_masks = private_protection_storage_masks;
        this.public_protection_storage_masks = public_protection_storage_masks;
    }

    public Optional<String> getPrivateKeyUniqueIdentifier() {
        return this.privateKeyUniqueIdentifier;
    }

    public void setPrivateKeyUniqueIdentifier(Optional<String> privateKeyUniqueIdentifier) {
        this.privateKeyUniqueIdentifier = privateKeyUniqueIdentifier;
    }

    public Optional<Integer> getOffset() {
        return this.offset;
    }

    public void setOffset(Optional<Integer> offset) {
        this.offset = offset;
    }

    public Optional<Attributes> getCommon_attributes() {
        return this.common_attributes;
    }

    public void setCommon_attributes(Optional<Attributes> common_attributes) {
        this.common_attributes = common_attributes;
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

    public Optional<ProtectionStorageMasks> getCommon_protection_storage_masks() {
        return this.common_protection_storage_masks;
    }

    public void setCommon_protection_storage_masks(Optional<ProtectionStorageMasks> common_protection_storage_masks) {
        this.common_protection_storage_masks = common_protection_storage_masks;
    }

    public Optional<ProtectionStorageMasks> getPrivate_protection_storage_masks() {
        return this.private_protection_storage_masks;
    }

    public void setPrivate_protection_storage_masks(Optional<ProtectionStorageMasks> private_protection_storage_masks) {
        this.private_protection_storage_masks = private_protection_storage_masks;
    }

    public Optional<ProtectionStorageMasks> getPublic_protection_storage_masks() {
        return this.public_protection_storage_masks;
    }

    public void setPublic_protection_storage_masks(Optional<ProtectionStorageMasks> public_protection_storage_masks) {
        this.public_protection_storage_masks = public_protection_storage_masks;
    }

    public ReKeyKeyPair privateKeyUniqueIdentifier(Optional<String> privateKeyUniqueIdentifier) {
        setPrivateKeyUniqueIdentifier(privateKeyUniqueIdentifier);
        return this;
    }

    public ReKeyKeyPair offset(Optional<Integer> offset) {
        setOffset(offset);
        return this;
    }

    public ReKeyKeyPair common_attributes(Optional<Attributes> common_attributes) {
        setCommon_attributes(common_attributes);
        return this;
    }

    public ReKeyKeyPair privateKeyAttributes(Optional<Attributes> privateKeyAttributes) {
        setPrivateKeyAttributes(privateKeyAttributes);
        return this;
    }

    public ReKeyKeyPair publicKeyAttributes(Optional<Attributes> publicKeyAttributes) {
        setPublicKeyAttributes(publicKeyAttributes);
        return this;
    }

    public ReKeyKeyPair common_protection_storage_masks(
        Optional<ProtectionStorageMasks> common_protection_storage_masks) {
        setCommon_protection_storage_masks(common_protection_storage_masks);
        return this;
    }

    public ReKeyKeyPair private_protection_storage_masks(
        Optional<ProtectionStorageMasks> private_protection_storage_masks) {
        setPrivate_protection_storage_masks(private_protection_storage_masks);
        return this;
    }

    public ReKeyKeyPair public_protection_storage_masks(
        Optional<ProtectionStorageMasks> public_protection_storage_masks) {
        setPublic_protection_storage_masks(public_protection_storage_masks);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ReKeyKeyPair)) {
            return false;
        }
        ReKeyKeyPair reKeyKeyPair = (ReKeyKeyPair) o;
        return Objects.equals(privateKeyUniqueIdentifier, reKeyKeyPair.privateKeyUniqueIdentifier)
            && Objects.equals(offset, reKeyKeyPair.offset)
            && Objects.equals(common_attributes, reKeyKeyPair.common_attributes)
            && Objects.equals(privateKeyAttributes, reKeyKeyPair.privateKeyAttributes)
            && Objects.equals(publicKeyAttributes, reKeyKeyPair.publicKeyAttributes)
            && Objects.equals(common_protection_storage_masks, reKeyKeyPair.common_protection_storage_masks)
            && Objects.equals(private_protection_storage_masks, reKeyKeyPair.private_protection_storage_masks)
            && Objects.equals(public_protection_storage_masks, reKeyKeyPair.public_protection_storage_masks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKeyUniqueIdentifier, offset, common_attributes, privateKeyAttributes,
            publicKeyAttributes, common_protection_storage_masks, private_protection_storage_masks,
            public_protection_storage_masks);
    }

    @Override
    public String toString() {
        return "{" + " privateKeyUniqueIdentifier='" + getPrivateKeyUniqueIdentifier() + "'" + ", offset='"
            + getOffset() + "'" + ", common_attributes='" + getCommon_attributes() + "'" + ", privateKeyAttributes='"
            + getPrivateKeyAttributes() + "'" + ", publicKeyAttributes='" + getPublicKeyAttributes() + "'"
            + ", common_protection_storage_masks='" + getCommon_protection_storage_masks() + "'"
            + ", private_protection_storage_masks='" + getPrivate_protection_storage_masks() + "'"
            + ", public_protection_storage_masks='" + getPublic_protection_storage_masks() + "'" + "}";
    }

}
