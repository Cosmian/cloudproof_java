package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum CertificateType {

    X509(0x01), PGP(0x02);

    private final int code;

    private CertificateType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, CertificateType> ENUM_MAP = KmipEnumUtils.to_map(CertificateType.values());

    public static CertificateType from(String name) throws IllegalArgumentException {
        CertificateType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No CertificateType  with name: " + name);
        }
        return o;
    }

    public static CertificateType from(int code) throws IllegalArgumentException {
        for (CertificateType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No CertificateType  with code: " + code);
    }
}
