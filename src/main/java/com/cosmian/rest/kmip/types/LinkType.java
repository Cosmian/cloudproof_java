package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum LinkType {

    /// For Certificate objects: the parent certificate for a certificate in a
    /// certificate chain. For Public Key objects: the corresponding certificate(s),
    /// containing the same public key.
    Certificate_Link(0x0000_0101),
    /// For a Private Key object: the public key corresponding to the private key.
    /// For a Certificate object: the public key contained in the certificate.
    Public_Key_Link(0x0000_0102),
    /// For a Public Key object: the private key corresponding to the public key.
    Private_Key_Link(0x0000_0103),
    /// For a derived Symmetric Key or Secret Data object: the object(s) from
    /// which the current symmetric key was derived.
    Derivation_Base_Object_Link(0x0000_0104),
    /// The symmetric key(s) or Secret Data object(s) that were derived from
    /// the current object.
    Derived_Key_Link(0x0000_0105),
    /// For a Symmetric Key, an Asymmetric Private Key, or an Asymmetric
    /// Public Key object: the key that resulted from the re-key of the current
    /// key. For a Certificate object: the certificate that resulted from the re-
    /// certify. Note that there SHALL be only one such replacement object per
    /// Managed Object.
    Replacement_Object_Link(0x0000_0106),
    /// For a Symmetric Key, an Asymmetric Private Key, or an Asymmetric
    /// Public Key object: the key that was re-keyed to obtain the current key.
    /// For a Certificate object: the certificate that was re-certified to obtain
    /// the
    /// current certificate.
    Replaced_Object_Link(0x0000_0107),
    /// For all object types: the container or other parent object corresponding
    /// to the object.
    Parent_Link(0x0000_0108),
    /// For all object types: the subordinate, derived or other child object
    /// corresponding to the object.
    Child_Link(0x0000_0109),
    /// For all object types: the previous object to this object.
    Previous_Link(0x0000_010A),
    /// For all object types: the next object to this object.
    Next_Link(0x0000_010B),
    PKCS_12_Certificate_Link(0x0000_010C),
    PKCS_12_Password_Link(0x0000_010D),
    /// For wrapped objects: the object that was used to wrap this object.
    Wrapping_Key_Link(0x0000_010E);

    // Extensions 8XXXXXXX
    private final int code;

    private LinkType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, LinkType> ENUM_MAP = KmipEnumUtils.to_map(LinkType.values());

    public static LinkType from(String name) throws IllegalArgumentException {
        LinkType o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No LinkType with name: " + name);
        }
        return o;
    }

    public static LinkType from(int code) throws IllegalArgumentException {
        for (LinkType value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No LinkType with code: " + code);
    }
}
