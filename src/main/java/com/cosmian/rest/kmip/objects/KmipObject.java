package com.cosmian.rest.kmip.objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.ObjectType;
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
     * @return
     * @throws CosmianException
     */
    public String toJson() throws CosmianException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CosmianException("Failed serializing to JSON the " + this.getObjectType() + ": " + e.getMessage(),
                    e);
        }
    }

    protected static <T extends KmipObject> T fromJson(String json, Class<T> clazz) throws CosmianException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            try {
                throw new CosmianException("Failed de serializing from JSON the " + clazz.newInstance().getObjectType()
                        + ": " + e.getMessage(), e);
            } catch (InstantiationException | IllegalAccessException e1) {
                throw new CosmianException("Failed de serializing from JSON:" + e.getMessage(), e);
            }
        }
    }

    /**
     * Helper function to retrieve the KMIP Object class from its {@link ObjectType}
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
