package com.cosmian.rest.kmip.types;

import com.cosmian.rest.kmip.json.KmipChoice2;

public class RevocationReason extends KmipChoice2<RevocationReasonEnumeration, String> {

    public RevocationReason(Object value) {
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

    @Override
    public String toString() {
        return super.toString();
    }

}