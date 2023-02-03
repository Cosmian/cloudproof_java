package com.cosmian.rest.kmip.operations;

import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.objects.KmipObject;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.KeyWrapType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests the server to Import a Managed Object specified by its Unique Identifier. The request
 * specifies the object being imported and all the attributes to be assigned to the object. The attribute rules for each
 * attribute for "Initially set by" and "When implicitly set" SHALL NOT be enforced as all attributes MUST be set to the
 * supplied values rather than any server generated values. The response contains the Unique Identifier provided in the
 * request or assigned by the server. The server SHALL copy the Unique Identifier returned by this operations into the
 * ID Placeholder variable. https://docs.oasis-open.org/kmip/kmip-spec/v2.1/os/kmip-spec-v2.1-os.html#_Toc57115657
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Import implements KmipStruct {

    /**
     * The Unique Identifier of the object to be imported
     */
    @JsonProperty(value = "UniqueIdentifier")
    private String uniqueIdentifier;

    /**
     * Determines the type of object being imported.
     */
    @JsonProperty(value = "ObjectType")
    private ObjectType objectType;

    /**
     * A Boolean. If specified and true then any existing object with the same Unique Identifier SHALL be replaced by
     * this operation. If absent or false and an object exists with the same Unique Identifier then an error SHALL be
     * returned.
     */
    @JsonProperty(value = "ReplaceExisting")
    private Optional<Boolean> replaceExisting;

    /**
     * If Not Wrapped then the server SHALL unwrap the object before storing it, and return an error if the wrapping key
     * is not available. Otherwise the server SHALL store the object as provided.
     */
    @JsonProperty(value = "KeyWrapType")
    private Optional<KeyWrapType> keyWrapType;

    /**
     * Specifies object attributes to be associated with the new object.
     */
    @JsonProperty("Attributes")
    private Attributes attributes;

    /**
     * The object being imported. The object and attributes MAY be wrapped.
     */
    @JsonProperty(value = "Object")
    private KmipObject object;

    public Import(String uniqueIdentifier, ObjectType objectType, Optional<Boolean> replaceExisting,
        Optional<KeyWrapType> keyWrapType, Attributes attributes, KmipObject object) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.objectType = objectType;
        this.replaceExisting = replaceExisting;
        this.keyWrapType = keyWrapType;
        this.attributes = attributes;
        this.object = object;
    }

    public Import() {
    }

    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String unique_identifier) {
        this.uniqueIdentifier = unique_identifier;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public void setObjectType(ObjectType object_type) {
        this.objectType = object_type;
    }

    public Optional<Boolean> getReplaceExisting() {
        return this.replaceExisting;
    }

    public void setReplaceExisting(Optional<Boolean> replace_existing) {
        this.replaceExisting = replace_existing;
    }

    public Optional<KeyWrapType> getKeyWrapType() {
        return this.keyWrapType;
    }

    public void setKeyWrapType(Optional<KeyWrapType> key_wrap_type) {
        this.keyWrapType = key_wrap_type;
    }

    public KmipObject getObject() {
        return object;
    }

    public void setObject(KmipObject object) {
        this.object = object;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

}
