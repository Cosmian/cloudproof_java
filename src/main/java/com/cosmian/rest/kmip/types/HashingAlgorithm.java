package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum HashingAlgorithm {

    MD2(0x0000_0001), MD4(0x0000_0002), MD5(0x0000_0003), SHA_1(0x0000_0004), SHA_224(0x0000_0005),
    SHA_256(0x0000_0006), SHA_384(0x0000_0007), SHA_512(0x0000_0008), RIPEMD_160(0x0000_0009), Tiger(0x0000_000A),
    Whirlpool(0x0000_000B), SHA_512_224(0x0000_000C), SHA_512_256(0x0000_000D), SHA3_224(0x0000_000E),
    SHA3_256(0x0000_000F), SHA3_384(0x0000_0010), SHA3_512(0x0000_0011);

    private final int code;

    private HashingAlgorithm(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, HashingAlgorithm> ENUM_MAP = KmipEnumUtils.to_map(HashingAlgorithm.values());

    public static HashingAlgorithm from(String name) throws IllegalArgumentException {
        HashingAlgorithm o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No HashingAlgorithm with name: " + name);
        }
        return o;
    }

    public static HashingAlgorithm from(int code) throws IllegalArgumentException {
        for (HashingAlgorithm value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No HashingAlgorithm with code: " + code);
    }
}
