package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

/**
 * the wrapped un-encoded value of the Byte String Key Material field
 */
public enum EncodingOption {

    /// the Key Value structure
    No_Encoding(0x0000_0001),
    /// the wrapped TTLV-encoded Key Value structure
    TTLV_Encoding(0x0000_0002);

    private final int code;

    private EncodingOption(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, EncodingOption> ENUM_MAP = KmipEnumUtils.to_map(EncodingOption.values());

    public static EncodingOption from(String name) throws IllegalArgumentException {
        EncodingOption o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No EncodingOption  with name: " + name);
        }
        return o;
    }

    public static EncodingOption from(int code) throws IllegalArgumentException {
        for (EncodingOption value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No EncodingOption  with code: " + code);
    }
}
