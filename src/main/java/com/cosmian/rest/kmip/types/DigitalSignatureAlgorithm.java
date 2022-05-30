package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum DigitalSignatureAlgorithm {

    MD2_with_RSA_Encryption(0x0000_0001),
    MD5_with_RSA_Encryption(0x0000_0002),
    SHA_1_with_RSA_Encryption(0x0000_0003),
    SHA_224_with_RSA_Encryption(0x0000_0004),
    SHA_256_with_RSA_Encryption(0x0000_0005),
    SHA_384_with_RSA_Encryption(0x0000_0006),
    SHA_512_with_RSA_Encryption(0x0000_0007),
    RSASSA_PSS(0x0000_0008),
    DSA_with_SHA_1(0x0000_0009),
    DSA_with_SHA224(0x0000_000A),
    DSA_with_SHA256(0x0000_000B),
    ECDSA_with_SHA_1(0x0000_000C),
    ECDSA_with_SHA224(0x0000_000D),
    ECDSA_with_SHA256(0x0000_000E),
    ECDSA_with_SHA384(0x0000_000F),
    ECDSA_with_SHA512(0x0000_0010),
    SHA3_256_with_RSA_Encryption(0x0000_0011),
    SHA3_384_with_RSA_Encryption(0x0000_0012),
    SHA3_512_with_RSA_Encryption(0x0000_0013);

    private final int code;

    private DigitalSignatureAlgorithm(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, DigitalSignatureAlgorithm> ENUM_MAP = KmipEnumUtils.to_map(DigitalSignatureAlgorithm.values());

    public static DigitalSignatureAlgorithm from(String name) throws IllegalArgumentException {
        DigitalSignatureAlgorithm o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No DigitalSignatureAlgorithm with name: " + name);
        }
        return o;
    }

    public static DigitalSignatureAlgorithm from(int code) throws IllegalArgumentException {
        for (DigitalSignatureAlgorithm value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No DigitalSignatureAlgorithm with code: " + code);
    }
}
