package com.cosmian.rest.kmip.data_structures;

import com.cosmian.rest.kmip.json.KmipChoice2;

/**
 * The Key Value is used only inside a Key Block and is either a Byte String or a: • The Key Value structure contains
 * the key material, either as a byte string or as a Transparent Key structure, and OPTIONAL attribute information that
 * is associated and encapsulated with the key material. This attribute information differs from the attributes
 * associated with Managed Objects, and is obtained via the Get Attributes operation, only by the fact that it is
 * encapsulated with (and possibly wrapped with) the key material itself. • The Key Value Byte String is either the
 * wrapped TTLV-encoded Key Value structure, or the wrapped un-encoded value of the Byte String Key Material field.
 */
public class KeyValue extends KmipChoice2<PlainTextKeyValue, byte[]> {

    public KeyValue(Object value) {
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
