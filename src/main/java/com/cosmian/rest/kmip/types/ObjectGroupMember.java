package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum ObjectGroupMember {

    GroupMemberFresh(0x0000_0001),
    GroupMemberDefault(0x0000_0002);

    private final int code;

    private ObjectGroupMember(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, ObjectGroupMember> ENUM_MAP = KmipEnumUtils.to_map(ObjectGroupMember.values());

    public static ObjectGroupMember from(String name) throws IllegalArgumentException {
        ObjectGroupMember o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No ObjectGroupMember with name: " + name);
        }
        return o;
    }

    public static ObjectGroupMember from(int code) throws IllegalArgumentException {
        for (ObjectGroupMember value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No ObjectType with code: " + code);
    }
}
