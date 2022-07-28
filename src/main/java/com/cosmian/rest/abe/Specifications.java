package com.cosmian.rest.abe;

import com.cosmian.rest.kmip.types.CryptographicAlgorithm;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.VendorAttribute;

public class Specifications {
    private Implementation implementation;

    public Specifications(Implementation implementation) {
        this.implementation = implementation;
    }

    public Implementation getImplementation() {
        return this.implementation;
    }

    public CryptographicAlgorithm getCryptographicAlgorithm() {
        if (implementation == Implementation.GPSW) {
            return CryptographicAlgorithm.ABE;
        } else {
            return CryptographicAlgorithm.CoverCrypt;
        }
    }

    public KeyFormatType getKeyFormatTypeMasterSecretKey() {
        if (implementation == Implementation.GPSW) {
            return KeyFormatType.AbeMasterSecretKey;
        } else {
            return KeyFormatType.CoverCryptSecretKey;
        }
    }

    public KeyFormatType getKeyFormatTypeDecryptionKey() {
        if (implementation == Implementation.GPSW) {
            return KeyFormatType.AbeUserDecryptionKey;
        } else {
            return KeyFormatType.CoverCryptSecretKey;
        }
    }

    public KeyFormatType getKeyFormatTypePublicKey() {
        if (implementation == Implementation.GPSW) {
            return KeyFormatType.AbeMasterPublicKey;
        } else {
            return KeyFormatType.CoverCryptPublicKey;
        }
    }

    public String getAccessPolicyVendorAttribute() {
        if (implementation == Implementation.GPSW) {
            return VendorAttribute.VENDOR_ATTR_ABE_ACCESS_POLICY;
        } else {
            return VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ACCESS_POLICY;
        }
    }

    public String getPolicyVendorAttribute() {
        if (implementation == Implementation.GPSW) {
            return VendorAttribute.VENDOR_ATTR_ABE_POLICY;
        } else {
            return VendorAttribute.VENDOR_ATTR_COVER_CRYPT_POLICY;
        }
    }

    public String getAbeAttrVendorAttribute() {
        if (implementation == Implementation.GPSW) {
            return VendorAttribute.VENDOR_ATTR_ABE_ATTR;
        } else {
            return VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ATTR;
        }
    }
}
