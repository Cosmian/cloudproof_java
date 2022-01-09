package com.cosmian.rest.kmip.objects;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.OpaqueDataType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpaqueObject extends KmipObject {

    @JsonProperty(value = "OpaqueDataType")
    private OpaqueDataType opaque_data_type;

    @JsonProperty(value = "OpaqueDataValue")
    private byte[] opaque_data_value;

    public OpaqueObject() {
    }

    public OpaqueObject(OpaqueDataType opaque_data_type, byte[] opaque_data_value) {
        this.opaque_data_type = opaque_data_type;
        this.opaque_data_value = opaque_data_value;
    }

    public OpaqueDataType getOpaque_data_type() {
        return this.opaque_data_type;
    }

    public void setOpaque_data_type(OpaqueDataType opaque_data_type) {
        this.opaque_data_type = opaque_data_type;
    }

    public byte[] getOpaque_data_value() {
        return this.opaque_data_value;
    }

    public void setOpaque_data_value(byte[] opaque_data_value) {
        this.opaque_data_value = opaque_data_value;
    }

    public OpaqueObject opaque_data_type(OpaqueDataType opaque_data_type) {
        setOpaque_data_type(opaque_data_type);
        return this;
    }

    public OpaqueObject opaque_data_value(byte[] opaque_data_value) {
        setOpaque_data_value(opaque_data_value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof OpaqueObject)) {
            return false;
        }
        OpaqueObject opaqueObject = (OpaqueObject) o;
        return Objects.equals(opaque_data_type, opaqueObject.opaque_data_type)
                && Arrays.equals(opaque_data_value, opaqueObject.opaque_data_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opaque_data_type, opaque_data_value);
    }

    @Override
    public String toString() {
        return "{" + " opaque_data_type='" + getOpaque_data_type() + "'" + ", opaque_data_value='"
                + getOpaque_data_value() + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.Opaque_Object;
    }

    /**
     * 
     * Deserialize an instance from its Json representation obtained using
     * {@link toJson()}
     */
    public static OpaqueObject fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, OpaqueObject.class);
    }
}
