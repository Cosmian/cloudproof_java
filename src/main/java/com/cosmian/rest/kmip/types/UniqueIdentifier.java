package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum UniqueIdentifier {

    ID_Placeholder(0x0000_0001),
    Certify(0x0000_0002),
    Create(0x0000_0003),
    Create_Key_Pair(0x0000_0004),
    Create_Key_Pair_Private_Key(0x0000_0005),
    Create_Key_Pair_Public_Key(0x0000_0006),
    Create_Split_Key(0x0000_0007),
    Derive_Key(0x0000_0008),
    Import(0x0000_0009),
    Join_Split_Key(0x0000_000A),
    Locate(0x0000_000B),
    Register(0x0000_000C),
    Re_key(0x0000_000D),
    Re_certify(0x0000_000E),
    Re_key_Key_Pair(0x0000_000F),
    Re_key_Key_Pair_Private_Key(0x0000_0010),
    Re_key_Key_Pair_Public_Key(0x0000_0011);

    private final int code;

    private UniqueIdentifier(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, UniqueIdentifier> ENUM_MAP = KmipEnumUtils.to_map(UniqueIdentifier.values());

    public static UniqueIdentifier from(String name) throws IllegalArgumentException {
        UniqueIdentifier o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No UniqueIdentifier with name: " + name);
        }
        return o;
    }

    public static UniqueIdentifier from(int code) throws IllegalArgumentException {
        for (UniqueIdentifier value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No UniqueIdentifier with code: " + code);
    }
}
