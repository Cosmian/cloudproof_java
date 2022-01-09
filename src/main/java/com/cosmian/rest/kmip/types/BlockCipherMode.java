package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum BlockCipherMode {

    CBC(0x0000_0001), ECB(0x0000_0002), PCBC(0x0000_0003), CFB(0x0000_0004), OFB(0x0000_0005), CTR(0x0000_0006),
    CMAC(0x0000_0007), CCM(0x0000_0008), GCM(0x0000_0009), CBC_MAC(0x0000_000A), XTS(0x0000_000B),
    X9_102_AESKW(0x0000_000E), X9_102_TDKW(0x0000_000F), X9_102_AKW1(0x0000_0010), X9_102_AKW2(0x0000_0011),
    AEAD(0x0000_0012);

    private final int code;

    private BlockCipherMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, BlockCipherMode> ENUM_MAP = KmipEnumUtils.to_map(BlockCipherMode.values());

    public static BlockCipherMode from(String name) throws IllegalArgumentException {
        BlockCipherMode o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No BlockCipherMode with name: " + name);
        }
        return o;
    }

    public static BlockCipherMode from(int code) throws IllegalArgumentException {
        for (BlockCipherMode value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No BlockCipherMode with code: " + code);
    }
}
