package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum SplitKeyMethod {

    XOR(0x00000001),
    PolynomialSharingGf2_16(0x0000_0002),
    PolynomialSharingPrimeField(0x0000_0003),
    PolynomialSharingGf2_8(0x0000_0004);

    private final int code;

    private SplitKeyMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, SplitKeyMethod> ENUM_MAP = KmipEnumUtils.to_map(SplitKeyMethod.values());

    public static SplitKeyMethod from(String name) throws IllegalArgumentException {
        SplitKeyMethod o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No SplitKeyMethod  with name: " + name);
        }
        return o;
    }

    public static SplitKeyMethod from(int code) throws IllegalArgumentException {
        for (SplitKeyMethod value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No SplitKeyMethod  with code: " + code);
    }
}
