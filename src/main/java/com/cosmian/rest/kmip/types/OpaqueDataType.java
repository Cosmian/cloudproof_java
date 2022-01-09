package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum OpaqueDataType {

    Unknown(0x8000_0001);

    private final int code;

    private OpaqueDataType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, OpaqueDataType> ENUM_MAP = KmipEnumUtils.to_map(OpaqueDataType.values());

    public static OpaqueDataType from(String name) throws IllegalArgumentException {
        OpaqueDataType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No OpaqueDataType  with name: " + name);
        }
        return o;
    }

    public static OpaqueDataType from(int code) throws IllegalArgumentException {
        for (OpaqueDataType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No OpaqueDataType  with code: " + code);
    }
}
