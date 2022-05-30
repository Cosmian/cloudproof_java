package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum KeyWrapType {

    Not_Wrapped(0x0000_0001),
    As_Registered(0x0000_0002);

    private final int code;

    private KeyWrapType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, KeyWrapType> ENUM_MAP = KmipEnumUtils.to_map(KeyWrapType.values());

    public static KeyWrapType from(String name) throws IllegalArgumentException {
        KeyWrapType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No KeyWrapType with name: " + name);
        }
        return o;
    }

    public static KeyWrapType from(int code) throws IllegalArgumentException {
        for (KeyWrapType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No ObjectType with code: " + code);
    }
}
