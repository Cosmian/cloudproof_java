package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum MaskGenerator {

    MFG1(0x0000_0001);

    private final int code;

    private MaskGenerator(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, MaskGenerator> ENUM_MAP = KmipEnumUtils.to_map(MaskGenerator.values());

    public static MaskGenerator from(String name) throws IllegalArgumentException {
        MaskGenerator o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No MaskGenerator with name: " + name);
        }
        return o;
    }

    public static MaskGenerator from(int code) throws IllegalArgumentException {
        for (MaskGenerator value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No MaskGenerator with code: " + code);
    }
}
