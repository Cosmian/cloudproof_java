package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum RevocationReasonEnumeration {

    Unspecified(0x0000_0001),
    KeyCompromise(0x0000_0002),
    CACompromise(0x0000_0003),
    AffiliationChanged(0x0000_0004),
    Superseded(0x0000_0005),
    CessationOfOperation(0x0000_0006),
    PrivilegeWithdrawn(0x0000_0007);

    private final int code;

    private RevocationReasonEnumeration(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, RevocationReasonEnumeration> ENUM_MAP =
        KmipEnumUtils.to_map(RevocationReasonEnumeration.values());

    public static RevocationReasonEnumeration from(String name) throws IllegalArgumentException {
        RevocationReasonEnumeration o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No RevocationReasonEnumeration  with name: " + name);
        }
        return o;
    }

    public static RevocationReasonEnumeration from(int code) throws IllegalArgumentException {
        for (RevocationReasonEnumeration value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No RevocationReasonEnumeration  with code: " + code);
    }
}
