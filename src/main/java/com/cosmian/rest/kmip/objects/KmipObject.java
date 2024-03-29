package com.cosmian.rest.kmip.objects;

import java.lang.reflect.InvocationTargetException;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public abstract class KmipObject implements KmipStruct {

    public abstract ObjectType getObjectType();

    /**
     * This method is mostly used for local tests and serialization.
     *
     * @return the JSON string
     * @throws CloudproofException if the serialization fails
     */
    public String toJson() throws CloudproofException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CloudproofException(
                "Failed serializing to JSON the " + this.getObjectType() + ": " + e.getMessage(),
                e);
        }
    }

    protected static <T extends KmipObject> T fromJson(String json,
                                                       Class<T> clazz)
        throws CloudproofException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            try {
                throw new CloudproofException("Failed de serializing from JSON the "
                    + clazz.getDeclaredConstructor().newInstance().getObjectType() + ": " + e.getMessage(), e);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e1) {
                throw new CloudproofException("Failed de serializing from JSON:" + e.getMessage(), e);
            }
        }
    }

    /**
     * Helper function to retrieve the KMIP Object class from its {@link ObjectType}
     *
     * @param objectType the {@link ObjectType}
     * @param <C> the {@link KmipObject}
     * @return the Class of the object
     */
    @SuppressWarnings("unchecked")
    public static <C extends KmipObject> Class<C> getObjectClass(ObjectType objectType) {
        if (objectType.equals(ObjectType.Certificate)) {
            return (Class<C>) Certificate.class;
        }
        if (objectType.equals(ObjectType.Certificate_Request)) {
            return (Class<C>) CertificateRequest.class;
        }
        if (objectType.equals(ObjectType.Opaque_Object)) {
            return (Class<C>) OpaqueObject.class;
        }
        if (objectType.equals(ObjectType.PGP_Key)) {
            return (Class<C>) PGPKey.class;
        }
        if (objectType.equals(ObjectType.Private_Key)) {
            return (Class<C>) PrivateKey.class;
        }
        if (objectType.equals(ObjectType.Public_Key)) {
            return (Class<C>) PublicKey.class;
        }
        if (objectType.equals(ObjectType.Secret_Data)) {
            return (Class<C>) SecretData.class;
        }
        if (objectType.equals(ObjectType.Split_Key)) {
            return (Class<C>) SplitKey.class;
        }
        if (objectType.equals(ObjectType.Symmetric_Key)) {
            return (Class<C>) SymmetricKey.class;
        }
        throw new IllegalArgumentException("Unsupported Object Type " + objectType + " for a KMIP Object");
    }

}
