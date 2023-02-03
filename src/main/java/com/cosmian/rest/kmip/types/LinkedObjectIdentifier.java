package com.cosmian.rest.kmip.types;

import com.cosmian.rest.kmip.json.KmipChoice3;

/**
 * Either: - String : Unique Identifier of a Managed Object - Enumeration: Zero based nth Unique Identifier in the
 * response. If negative the count is backwards from the beginning of the current operation's batch item. - Integer:
 * Index
 */
public class LinkedObjectIdentifier extends KmipChoice3<String, UniqueIdentifier, Integer> {

    public LinkedObjectIdentifier(Object value) {
        super(value);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
