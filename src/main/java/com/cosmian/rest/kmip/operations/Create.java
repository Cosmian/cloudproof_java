package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests the server to generate a new symmetric key or
 * generate Secret Data as a Managed Cryptographic Object. The request contains
 * information about the type of object being created, and some of the
 * attributes to be assigned to the object (e.g., Cryptographic Algorithm,
 * Cryptographic Length, etc.). The response contains the Unique Identifier of
 * the created object. The server SHALL copy the Unique Identifier returned by
 * this operation into the ID Placeholder variable.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Create implements KmipStruct {

    /// Determines the type of object to be created.
    @JsonProperty(value = "ObjectType")
    private ObjectType objectType;

    /// Specifies desired attributes to be associated with the new object.
    @JsonProperty("Attributes")
    private Attributes attributes;

    /// Specifies all permissible Protection Storage Mask selections for the new
    /// object
    /// @see ProtectionStorageMasks
    @JsonProperty(value = "ProtectionStorageMasks")
    private Optional<Integer> protection_storage_masks;

    public Create() {
    }

    public Create(ObjectType objectType, Attributes attributes, Optional<Integer> protection_storage_masks) {
        this.objectType = objectType;
        this.attributes = attributes;
        this.protection_storage_masks = protection_storage_masks;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public Attributes getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Optional<Integer> getProtection_storage_masks() {
        return this.protection_storage_masks;
    }

    public void setProtection_storage_masks(Optional<Integer> protection_storage_masks) {
        this.protection_storage_masks = protection_storage_masks;
    }

    public Create objectType(ObjectType objectType) {
        setObjectType(objectType);
        return this;
    }

    public Create attributes(Attributes attributes) {
        setAttributes(attributes);
        return this;
    }

    public Create protection_storage_masks(Optional<Integer> protection_storage_masks) {
        setProtection_storage_masks(protection_storage_masks);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Create)) {
            return false;
        }
        Create create = (Create) o;
        return Objects.equals(objectType, create.objectType) && Objects.equals(attributes, create.attributes)
                && Objects.equals(protection_storage_masks, create.protection_storage_masks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, attributes, protection_storage_masks);
    }

    @Override
    public String toString() {
        return "{" + " objectType='" + getObjectType() + "'" + ", attributes='" + getAttributes() + "'"
                + ", protection_storage_masks='" + getProtection_storage_masks() + "'" + "}";
    }

}
