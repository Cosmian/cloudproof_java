package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum KeyRoleType {

    BDK(0x0000_0001),
    CVK(0x0000_0002),
    DEK(0x0000_0003),
    MKAC(0x0000_0004),
    MKSMC(0x0000_0005),
    MKSMI(0x0000_0006),
    MKDAC(0x0000_0007),
    MKDN(0x0000_0008),
    MKCP(0x0000_0009),
    MKOTH(0x0000_000A),
    KEK(0x0000_000B),
    MAC16609(0x0000_000C),
    MAC97971(0x0000_000D),
    MAC97972(0x0000_000E),
    MAC97973(0x0000_000F),
    MAC97974(0x0000_0010),
    MAC97975(0x0000_0011),
    ZPK(0x0000_0012),
    PVKIBM(0x0000_0013),
    PVKPVV(0x0000_0014),
    PVKOTH(0x0000_0015),
    DUKPT(0x0000_0016),
    IV(0x0000_0017),
    TRKBK(0x0000_0018);

    private final int code;

    private KeyRoleType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, KeyRoleType> ENUM_MAP = KmipEnumUtils.to_map(KeyRoleType.values());

    public static KeyRoleType from(String name) throws IllegalArgumentException {
        KeyRoleType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No KeyRoleType with name: " + name);
        }
        return o;
    }

    public static KeyRoleType from(int code) throws IllegalArgumentException {
        for (KeyRoleType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No KeyRoleType with code: " + code);
    }
}
