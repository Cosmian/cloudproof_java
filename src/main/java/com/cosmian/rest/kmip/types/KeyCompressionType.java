package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum KeyCompressionType {

    EC_Public_Key_Type_Uncompressed(0x0000_0001), EC_Public_Key_Type_X9_62_Compressed_Prime(0x0000_0002),
    EC_Public_Key_Type_X9_62_Compressed_Char2(0x0000_0003), EC_Public_Key_Type_X9_62_Hybrid(0x0000_0004);

    private final int code;

    private KeyCompressionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, KeyCompressionType> ENUM_MAP = KmipEnumUtils.to_map(KeyCompressionType.values());

    public static KeyCompressionType from(String name) throws IllegalArgumentException {
        KeyCompressionType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No KeyCompressionType  with name: " + name);
        }
        return o;
    }

    public static KeyCompressionType from(int code) throws IllegalArgumentException {
        for (KeyCompressionType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No KeyCompressionType  with code: " + code);
    }
}
