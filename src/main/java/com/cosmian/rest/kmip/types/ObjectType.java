package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum ObjectType {

    Certificate(0x0000_0001),
    Symmetric_Key(0x0000_0002),
    Public_Key(0x0000_0003),
    Private_Key(0x0000_0004),
    Split_Key(0x0000_0005),
    Secret_Data(0x0000_0007),
    Opaque_Object(0x0000_0008),
    PGP_Key(0x0000_0009),
    Certificate_Request(0x0000_000A);

    private final int code;

    private ObjectType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, ObjectType> ENUM_MAP = KmipEnumUtils.to_map(ObjectType.values());

    public static ObjectType from(String name) throws IllegalArgumentException {
        ObjectType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No ObjectType with name: " + name);
        }
        return o;
    }

    public static ObjectType from(int code) throws IllegalArgumentException {
        for (ObjectType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No ObjectType with code: " + code);
    }
}
