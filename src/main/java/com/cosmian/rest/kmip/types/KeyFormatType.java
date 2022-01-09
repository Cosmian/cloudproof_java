package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum KeyFormatType {

    Raw(0x01), Opaque(0x02), PKCS1(0x03), PKCS8(0x04), X509(0x05), ECPrivateKey(0x06), TransparentSymmetricKey(0x07),
    TransparentDSAPrivateKey(0x08), TransparentDSAPublicKey(0x09), TransparentRSAPrivateKey(0x0A),
    TransparentRSAPublicKey(0x0B), TransparentDHPrivateKey(0x0C), TransparentDHPublicKey(0x0D),
    TransparentECPrivateKey(0x14), TransparentECPublicKey(0x15), PKCS12(0x016), PKCS10(0x17),
    McfeSecretKey(0x8880_0001), McfeMasterSecretKey(0x8880_0002), McfeFunctionalKey(0x8880_0003),
    McfeFksSecretKey(0x8880_0004), EnclaveECKeyPair(0x8880_0005), EnclaveECSharedKey(0x8880_0006), TFHE(0x8880_0007),
    AbeMasterSecretKey(0x8880_0008), AbeMasterPublicKey(0x8880_0009), AbeUserDecryptionKey(0x8880_000A),
    AbeSymmetricKey(0x8880_000B);

    private final int code;

    private KeyFormatType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, KeyFormatType> ENUM_MAP = KmipEnumUtils.to_map(KeyFormatType.values());

    public static KeyFormatType from(String name) throws IllegalArgumentException {
        KeyFormatType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No KeyFormatType with name: " + name);
        }
        return o;
    }

    public static KeyFormatType from(int code) throws IllegalArgumentException {
        for (KeyFormatType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No KeyFormatType with code: " + code);
    }
}
