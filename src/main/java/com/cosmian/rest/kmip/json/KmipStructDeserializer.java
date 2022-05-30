package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Logger;

import com.cosmian.rest.kmip.objects.KmipObject;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.operations.Import;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class KmipStructDeserializer<E extends KmipStruct> extends KmipJsonDeserializer<E> {

    private static final Logger logger = Logger.getLogger(KmipStructDeserializer.class.getName());

    private final Class<?> handled_type;

    // needed by Jackson when using @JsonDeserializer
    public KmipStructDeserializer() {
        this(null);
    }

    public KmipStructDeserializer(Class<E> handled_type) {
        this.handled_type = handled_type;
    }

    @Override
    public Class<?> handledType() {
        return this.handled_type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E deserialize(JsonNode node, DeserializationContext context) throws IOException, JacksonException {

        // recover instance from tag
        JsonNode tag_node = node.get("tag");
        if (tag_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No tag");
        }
        E instance = this.handledType() == null ? get_instance_from_tag(tag_node.asText())
            : get_instance_from_class((Class<E>) this.handledType());
        logger.finer(() -> "Deserializing a " + instance.getClass().getName());

        // extract values
        JsonNode value_node = node.get("value");
        if (value_node == null) {
            throw new IOException("Invalid KMIP Json " + node.toPrettyString() + ". No value");
        }

        // Cache the parsed Object Type if a KmipObject must be deserialized
        ObjectType objectType = null;

        for (ResolvedField field : KmipJson.fields(instance.getClass())) {

            final ResolvedType resolvedType = field.getType();
            final boolean is_optional = resolvedType.getErasedType().equals(Optional.class);
            Class<?> resolved_class =
                is_optional ? resolvedType.getTypeParameters().get(0).getErasedType() : resolvedType.getErasedType();
            if (KmipObject.class.isAssignableFrom(resolved_class)) {
                // in the case the class is the KMIP Object, resolve the actual class
                if (objectType == null) {
                    throw new IllegalArgumentException("Unable to deserialize a KMIP Object: the type is not known");
                }
                resolved_class = KmipObject.getObjectClass(objectType);
            }
            final Class<?> clazz = resolved_class;

            Field f = field.getRawMember();

            // we only care about fields and ignore getters and setters
            f.setAccessible(true);
            JsonProperty property = f.getAnnotation(JsonProperty.class);
            String tag = property == null ? clazz.getSimpleName() : property.value();
            logger.finer(() -> " ... processing " + (is_optional ? "optional " : "required ") + tag + " as a " + clazz);

            JsonNode field_node = null;
            for (JsonNode n : value_node) {
                JsonNode tag_n = n.get("tag");
                if (tag_n == null) {
                    throw new IOException("Invalid KMIP Json " + n.toPrettyString() + ". No tag");
                }
                if (tag_n.asText().equals(tag)) {
                    // found the corresponding Json, call the deserializer
                    field_node = n;
                    break;
                }
            }

            try {
                if (field_node == null) {
                    if (is_optional) {
                        f.set(instance, Optional.empty());
                    } else {
                        throw new IOException("Invalid KMIP Json for " + instance.getClass().getName()
                            + ". No json for required property " + tag + ".\n" + node.toPrettyString());
                    }
                } else {
                    Object field_value = KmipJson.deserialize_value(clazz, field_node, context);
                    if (is_optional) {
                        f.set(instance, Optional.of(field_value));
                    } else {
                        f.set(instance, field_value);
                    }
                    if (field_value instanceof ObjectType) {
                        // we need to cache the object type so that a KMIP Object knows what to
                        // deserialize to
                        objectType = (ObjectType) field_value;
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IOException("Failed to access field " + tag + ": " + e.getMessage(), e);
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private <K extends KmipStruct> K get_instance_from_tag(String tag) throws IOException {
        try {
            // try as an operation (use Import for package)
            return (K) Class.forName(Import.class.getPackage().getName() + "." + tag).getDeclaredConstructor()
                .newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException | ClassNotFoundException e1) {

            try {
                // try as an object (use PrivateKey for package)
                return (K) Class.forName(PrivateKey.class.getPackage().getName() + "." + tag).getDeclaredConstructor()
                    .newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e2) {

                throw new IOException("Cannot deserialize KMIP unknown tag: " + tag + ": " + e2.getMessage(), e2);
            }
        }
    }

    private <K extends KmipStruct> K get_instance_from_class(Class<K> clazz) throws IOException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
            throw new IOException("Cannot instantiate KMIP class: " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }

}
