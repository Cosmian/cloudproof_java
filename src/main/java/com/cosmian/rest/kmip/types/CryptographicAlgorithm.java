package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum CryptographicAlgorithm {

    DES(0x0000_0001), THREE_DES(0x0000_0002), AES(0x0000_0003), RSA(0x0000_0004), DSA(0x0000_0005), ECDSA(0x0000_0006),
    HMAC_SHA1(0x0000_0007), HMAC_SHA224(0x0000_0008), HMAC_SHA256(0x0000_0009), HMAC_SHA384(0x0000_000A),
    HMAC_SHA512(0x0000_000B), HMAC_MD5(0x0000_000C), DH(0x0000_000D), ECMQV(0x0000_000F), Blowfish(0x0000_0010),
    Camellia(0x0000_0011), CAST5(0x0000_0012), IDEA(0x0000_0013), MARS(0x0000_0014), RC2(0x0000_0015), RC4(0x0000_0016),
    RC5(0x0000_0017), SKIPJACK(0x0000_0018), Twofish(0x0000_0019), EC(0x0000_001A), One_Time_Pad(0x0000_001B),
    ChaCha20(0x0000_001C), Poly1305(0x0000_001D), ChaCha20Poly1305(0x0000_001E), SHA3_224(0x0000_001F),
    SHA3_256(0x0000_0020), SHA3_384(0x0000_0021), SHA3_512(0x0000_0022), HMAC_SHA3_224(0x0000_0023),
    HMAC_SHA3_256(0x0000_0024), HMAC_SHA3_384(0x0000_0025), HMAC_SHA3_512(0x0000_0026), SHAKE_128(0x0000_0027),
    SHAKE_256(0x0000_0028), ARIA(0x0000_0029), SEED(0x0000_002A), SM2(0x0000_002B), SM3(0x0000_002C), SM4(0x0000_002D),
    GOST_R_34_10_2012(0x0000_002E), GOST_R_34_11_2012(0x0000_002F), GOST_R_34_13_2015(0x0000_0030),
    GOST_28147_89(0x0000_0031), XMSS(0x0000_0032), SPHINCS_256(0x0000_0033), Page_166_of_230McEliece(0x0000_0034),
    McEliece_6960119(0x0000_0035), McEliece_8192128(0x0000_0036), Ed25519(0x0000_0037), Ed448(0x0000_0038),
    LWE(0x8880_0001), TFHE(0x8880_0002), ABE(0x8880_0003);

    private final int code;

    private CryptographicAlgorithm(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, CryptographicAlgorithm> ENUM_MAP = KmipEnumUtils.to_map(CryptographicAlgorithm.values());

    public static CryptographicAlgorithm from(String name) throws IllegalArgumentException {
        CryptographicAlgorithm o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No CryptographicAlgorithm with name: " + name);
        }
        return o;
    }

    public static CryptographicAlgorithm from(int code) throws IllegalArgumentException {
        for (CryptographicAlgorithm value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No ObjectType with code: " + code);
    }
}
