package com.cosmian.rest.kmip.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class KmipStructSerializer<E extends Object> extends JsonSerializer<E> {

    private static final Logger logger = Logger.getLogger(KmipStructSerializer.class.getName());

    private final String tag;

    public KmipStructSerializer(String tag) {
        this.tag = tag;
    }

    // required by @JsonSerialize annotation
    public KmipStructSerializer() {
        this(null);
    }

    @Override
    public void serialize(E struct, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("tag");
        generator.writeString(this.tag == null ? struct.getClass().getSimpleName() : this.tag);
        generator.writeFieldName("value");
        generator.writeStartArray();
        Class<?> clazz = struct.getClass();

        logger.finer(() -> "Serializing a " + clazz.getName());

        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {

            JsonIgnore ignore = f.getAnnotation(JsonIgnore.class);
            if (ignore != null) {
                continue;
            }

            f.setAccessible(true);
            Object v;
            try {
                v = f.get(struct);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IOException("Cannot serialize: " + clazz.getName() + ": unable to access value of: "
                    + f.getName() + ": " + e.getMessage(), e);
            }
            if (v == null) {
                throw new IOException(
                    "Cannot serialize: " + clazz.getName() + ": value of: " + f.getName() + " should not be NULL ");
            }
            // is it an optional field ?
            boolean is_optional = (v instanceof Optional);
            // unwrap v
            if (is_optional) {
                Optional<?> opt = (Optional<?>) v;
                if (opt.isPresent()) {
                    v = opt.get();
                } else {
                    // empty optional: do nos serialize
                    continue;
                }
            }
            final Object value = v;
            JsonProperty property = f.getAnnotation(JsonProperty.class);
            String tag = property == null ? value.getClass().getSimpleName() : property.value();
            logger.finer(
                () -> " ... processing " + (is_optional ? "optional " : "required ") + tag + " with value " + value);
            KmipJson.serialize_value(tag, value, generator, serializers);
        }
        generator.writeEndArray();
        generator.writeEndObject();

    }

}
