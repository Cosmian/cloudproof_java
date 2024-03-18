package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.cosmian.rest.kmip.objects.KmipObject;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Utility class
 */
public class KmipJson {

    // private static final Logger logger =
    // Logger.getLogger(KmipJson.class.getName());

    private final static TypeResolver type_resolver = new TypeResolver();

    public final static TypeResolver getTypeResolver() {
        return type_resolver;
    }

    public static ResolvedField[] fields(Class<?> clazz) {
        ResolvedType resolved_struct = type_resolver.resolve(clazz);
        MemberResolver member_resolver = new MemberResolver(type_resolver);
        ResolvedTypeWithMembers resolved_struct_with_members = member_resolver.resolve(resolved_struct, null, null);
        return resolved_struct_with_members.getMemberFields();
    }

    public static Class<?>[] type_parameters(Class<?> clazz) {
        ResolvedType resolved_type = type_resolver.resolve(clazz);
        List<Class<?>> classes = resolved_type.getTypeParameters().stream().map((ResolvedType e) -> {
            return e.getErasedType();
        }).collect(Collectors.toList());
        return classes.toArray(new Class<?>[classes.size()]);
    }

    public static Class<?>[] type_parameters_for_super_class(Class<?> clazz,
                                                             Class<?> super_class) {
        ResolvedType resolved_type = type_resolver.resolve(clazz);
        List<Class<?>> classes = resolved_type.typeParametersFor(super_class).stream().map((ResolvedType e) -> {
            return e.getErasedType();
        }).collect(Collectors.toList());
        return classes.toArray(new Class<?>[classes.size()]);
    }

    public static <E> E deserialize_value(Class<E> clazz,
                                          JsonNode node,
                                          DeserializationContext context)
        throws IllegalArgumentException, IOException {
        try {
            return deserializer_from_class(clazz).deserialize(node, context);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                "Unable to deserialize value for class: " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> KmipJsonDeserializer<E> deserializer_from_class(final Class<E> clazz)
        throws IllegalArgumentException {
        // logger.finer(() -> "Searching deserializer for " + clazz.getName() + "...");
        KmipJsonDeserializer<?> deserializer = null;
        if (clazz.isEnum()) {
            deserializer = new KmipEnumDeserializer((Class<Enum<?>>) clazz);
        } else if (clazz.equals(String.class)) {
            deserializer = new KmipStringDeserializer();
        } else if (clazz.equals(Boolean.class)) {
            deserializer = new KmipBooleanDeserializer();
        } else if (clazz.equals(boolean.class)) {
            deserializer = new KmipBooleanDeserializer();
        } else if (clazz.equals(Integer.class)) {
            deserializer = new KmipIntegerDeserializer();
        } else if (clazz.equals(int.class)) {
            deserializer = new KmipIntegerDeserializer();
        } else if (clazz.equals(byte[].class)) {
            deserializer = new KmipBytesDeserializer();
        } else if (KmipObject.class.isAssignableFrom(clazz)) {
            deserializer = new KmipStructDeserializer<KmipStruct>((Class<KmipStruct>) clazz);
        } else if (clazz.getSuperclass() != null && clazz.getSuperclass().equals(KmipChoice2.class)) {
            deserializer = new KmipChoice2Deserializer<KmipChoice2<?, ?>>((Class<KmipChoice2<?, ?>>) clazz);
        } else if (clazz.getSuperclass() != null && clazz.getSuperclass().equals(KmipChoice3.class)) {
            deserializer = new KmipChoice3Deserializer<KmipChoice3<?, ?, ?>>((Class<KmipChoice3<?, ?, ?>>) clazz);
        } else if (clazz.getSuperclass() != null && clazz.getSuperclass().equals(KmipChoice6.class)) {
            deserializer = new KmipChoice6Deserializer<KmipChoice6<?, ?, ?, ?, ?, ?>>(
                (Class<KmipChoice6<?, ?, ?, ?, ?, ?>>) clazz);
        } else if (KmipStruct.class.isAssignableFrom(clazz)) {
            deserializer = new KmipStructDeserializer<KmipStruct>((Class<KmipStruct>) clazz);
        } else if (clazz.isArray()) {
            deserializer = new KmipArrayDeserializer<E>((Class<E[]>) clazz);
        } else {
            throw new IllegalArgumentException("No KMIP deserializer for class " + clazz.getName());
        }
        // logger.finer("... found " + deserializer.getClass().getName());
        return (KmipJsonDeserializer<E>) deserializer;
    }

    @SuppressWarnings("unchecked")
    public static void serialize_value(String tag,
                                       Object value,
                                       JsonGenerator generator,
                                       SerializerProvider serializers)
        throws IllegalArgumentException {
        try {
            if (value instanceof Enum<?>) {
                new KmipEnumSerializer((Class<Enum<?>>) value.getClass(), tag).serialize((Enum<?>) value, generator,
                    serializers);
            } else if (value instanceof String) {
                new KmipStringSerializer(tag).serialize((String) value, generator, serializers);
            } else if (value instanceof Boolean) {
                new KmipBooleanSerializer(tag).serialize((Boolean) value, generator, serializers);
            } else if (value instanceof Integer) {
                new KmipIntegerSerializer(tag).serialize((Integer) value, generator, serializers);
            } else if (value instanceof byte[]) {
                new KmipBytesSerializer(tag).serialize((byte[]) value, generator, serializers);
            } else if (value instanceof KmipChoice2) {
                new KmipChoice2Serializer(tag).serialize((KmipChoice2<?, ?>) value, generator, serializers);
            } else if (value instanceof KmipChoice3) {
                new KmipChoice3Serializer(tag).serialize((KmipChoice3<?, ?, ?>) value, generator, serializers);
            } else if (value instanceof KmipChoice6) {
                new KmipChoice6Serializer(tag).serialize((KmipChoice6<?, ?, ?, ?, ?, ?>) value, generator, serializers);
            } else if (KmipStruct.class.isAssignableFrom(value.getClass())) {
                new KmipStructSerializer<KmipStruct>(tag).serialize((KmipStruct) value, generator, serializers);
            } else if (value.getClass().isArray()) {
                new KmipArraySerializer<Object>(tag).serialize((Object[]) value, generator, serializers);
            } else {
                throw new IllegalArgumentException("No KMIP serializer for class " + value.getClass().getName());
            }
        } catch (IllegalArgumentException |

            IOException e) {
            throw new IllegalArgumentException("Unable to serialize: " + tag + ": " + e.getMessage(), e);
        }
    }

}
