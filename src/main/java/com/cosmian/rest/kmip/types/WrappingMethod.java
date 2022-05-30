package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum WrappingMethod {

    Encrypt(0x0000_0001),
    MAC_sign(0x0000_0002),
    Encrypt_then_MAC_sign(0x0000_0003),
    MAC_sign_then_encrypt(0x0000_0004),
    TR_31(0x0000_0005);

    private final int code;

    private WrappingMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, WrappingMethod> ENUM_MAP = KmipEnumUtils.to_map(WrappingMethod.values());

    public static WrappingMethod from(String name) throws IllegalArgumentException {
        WrappingMethod o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No WrappingMethod  with name: " + name);
        }
        return o;
    }

    public static WrappingMethod from(int code) throws IllegalArgumentException {
        for (WrappingMethod value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No WrappingMethod  with code: " + code);
    }
}
