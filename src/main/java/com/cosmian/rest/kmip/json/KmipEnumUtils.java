package com.cosmian.rest.kmip.json;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KmipEnumUtils {

    public static String to_string(Enum<?> e) {
        return e.name().replace("_", "");
    }

    public static <E extends Enum<E>> Map<String, E> to_map(E[] e) {
        return Stream.of(e).collect(Collectors.toMap(v -> to_string(v), Function.identity()));
    }

}