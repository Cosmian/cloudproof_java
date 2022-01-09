package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum SecretDataType {

    Password(0x01), Seed(0x02), FunctionalKey(0x8000_0001), FunctionalKeyShare(0x8000_0002);

    private final int code;

    private SecretDataType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, SecretDataType> ENUM_MAP = KmipEnumUtils.to_map(SecretDataType.values());

    public static SecretDataType from(String name) throws IllegalArgumentException {
        SecretDataType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No SecretDataType  with name: " + name);
        }
        return o;
    }

    public static SecretDataType from(int code) throws IllegalArgumentException {
        for (SecretDataType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No SecretDataType  with code: " + code);
    }
}
