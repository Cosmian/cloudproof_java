package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum CertificateRequestType {

    CRMF(0x01),
    PKCS10(0x02),
    PEM(0x03);

    private final int code;

    private CertificateRequestType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, CertificateRequestType> ENUM_MAP = KmipEnumUtils.to_map(CertificateRequestType.values());

    public static CertificateRequestType from(String name) throws IllegalArgumentException {
        CertificateRequestType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No CertificateRequestType  with name: " + name);
        }
        return o;
    }

    public static CertificateRequestType from(int code) throws IllegalArgumentException {
        for (CertificateRequestType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No CertificateRequestType  with code: " + code);
    }
}
