package com.cosmian.rest.kmip.json;

import java.io.IOException;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipChoice3Deserializer<E extends KmipChoice3<?, ?, ?>> extends KmipJsonDeserializer<E> {

    private static final Logger logger = Logger.getLogger(KmipChoice3Deserializer.class.getName());

    private final Class<?> handled_type;

    public KmipChoice3Deserializer(Class<E> handled_type) {
        this.handled_type = handled_type;
    }

    @Override
    public Class<?> handledType() {
        return this.handled_type;
    }

    @Override
    public E deserialize(JsonNode node, DeserializationContext context) throws IOException, JacksonException {

        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>) this.handledType();

        // logger.finer("Deserializing a " + clazz.getName() + ", value: " +
        // p.getCodec().readTree(p));

        // Recover the types on the Choice and try deserializing against them
        for (Class<?> p_class : KmipJson.type_parameters_for_super_class(clazz, KmipChoice3.class)) {

            try {
                Object value = KmipJson.deserialize_value(p_class, node, context);
                E instance = clazz.getDeclaredConstructor(Object.class).newInstance(value);
                logger.finer("Deserializing a " + clazz.getName() + ": to a " + instance.getClass().getName() + "   ");
                return instance;
            } catch (Exception e) {
                logger.finer(
                        "Deserializing a " + clazz.getName() + ": not a " + p_class.getName() + "   " + e.getMessage());
                continue;
            }
        }

        throw new IOException(
                "Unable to deserialize a " + clazz.getName() + " for the value: " + node.toPrettyString());
    }

}
