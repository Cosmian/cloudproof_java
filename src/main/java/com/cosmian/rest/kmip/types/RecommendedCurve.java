package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum RecommendedCurve {

    P_192(0x0000_0001),
    K_163(0x0000_0002),
    B_163(0x0000_0003),
    P_224(0x0000_0004),
    K_233(0x0000_0005),
    B_233(0x0000_0006),
    P_256(0x0000_0007),
    K_283(0x0000_0008),
    B_283(0x0000_0009),
    P_384(0x0000_000A),
    K_409(0x0000_000B),
    B_409(0x0000_000C),
    P_521(0x0000_000D),
    K_571(0x0000_000E),
    B_571(0x0000_000F),
    SECP112R1(0x0000_0010),
    SECP112R2(0x0000_0011),
    SECP128R1(0x0000_0012),
    SECP128R2(0x0000_0013),
    SECP160K1(0x0000_0014),
    SECP160R1(0x0000_0015),
    SECP160R2(0x0000_0016),
    SECP192K1(0x0000_0017),
    SECP224K1(0x0000_0018),
    SECP256K1(0x0000_0019),
    SECT113R1(0x0000_001A),
    SECT131R1(0x0000_001C),
    SECT131R2(0x0000_001D),
    SECT163R1(0x0000_001E),
    SECT193R1(0x0000_001F),
    SECT193R2(0x0000_0020),
    SECT239K1(0x0000_0021),
    ANSIX9P192V2(0x0000_0022),
    ANSIX9P192V3(0x0000_0023),
    ANSIX9P239V1(0x0000_0024),
    ANSIX9P239V2(0x0000_0025),
    ANSIX9P239V3(0x0000_0026),
    ANSIX9C2PNB163V1(0x0000_0027),
    ANSIX9C2PNB163V2(0x0000_0028),
    ANSIX9C2PNB163V3(0x0000_0029),
    ANSIX9C2PNB176V1(0x0000_002A),
    ANSIX9C2TNB191V1(0x0000_002B),
    ANSIX9C2TNB191V2(0x0000_002C),
    ANSIX9C2TNB191V3(0x0000_002D),
    ANSIX9C2PNB208W1(0x0000_002E),
    ANSIX9C2TNB239V1(0x0000_002F),
    ANSIX9C2TNB239V2(0x0000_0030),
    ANSIX9C2TNB239V3(0x0000_0031),
    ANSIX9C2PNB272W1(0x0000_0032),
    ANSIX9C2PNB304W1(0x0000_0033),
    ANSIX9C2TNB359V1(0x0000_0034),
    ANSIX9C2PNB368W1(0x0000_0035),
    ANSIX9C2TNB431R1(0x0000_0036),
    BRAINPOOLP160R1(0x0000_0037),
    BRAINPOOLP160T1(0x0000_0038),
    BRAINPOOLP192R1(0x0000_0039),
    BRAINPOOLP192T1(0x0000_003A),
    BRAINPOOLP224R1(0x0000_003B),
    BRAINPOOLP224T1(0x0000_003C),
    BRAINPOOLP256R1(0x0000_003D),
    BRAINPOOLP256T1(0x0000_003E),
    BRAINPOOLP320R1(0x0000_003F),
    BRAINPOOLP320T1(0x0000_0040),
    BRAINPOOLP384T1(0x0000_0042),
    BRAINPOOLP512R1(0x0000_0043),
    BRAINPOOLP512T1(0x0000_0044),
    CURVE25519(0x0000_0045),
    CURVE448(0x0000_0046);
    // Extensions 8XXXXXXX

    private final int code;

    private RecommendedCurve(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, RecommendedCurve> ENUM_MAP = KmipEnumUtils.to_map(RecommendedCurve.values());

    public static RecommendedCurve from(String name) throws IllegalArgumentException {
        RecommendedCurve o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No RecommendedCurve with name: " + name);
        }
        return o;
    }

    public static RecommendedCurve from(int code) throws IllegalArgumentException {
        for (RecommendedCurve value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No RecommendedCurve with code: " + code);
    }
}
