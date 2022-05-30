package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum PaddingMethod {

    None(0x0000_0001),
    OAEP(0x0000_0002),
    PKCS5(0x0000_0003),
    SSL3(0x0000_0004),
    Zeros(0x0000_0005),
    ANSI_X9_23(0x0000_0006),
    ISO_10126(0x0000_0007),
    PKCS1_v1_5(0x0000_0008),
    X9_31(0x0000_0009),
    PSS(0x0000_000A);

    private final int code;

    private PaddingMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, PaddingMethod> ENUM_MAP = KmipEnumUtils.to_map(PaddingMethod.values());

    public static PaddingMethod from(String name) throws IllegalArgumentException {
        PaddingMethod o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No PaddingMethod with name: " + name);
        }
        return o;
    }

    public static PaddingMethod from(int code) throws IllegalArgumentException {
        for (PaddingMethod value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No PaddingMethod with code: " + code);
    }
}
