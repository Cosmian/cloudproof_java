package com.cosmian.rest.kmip.objects;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.types.CertificateType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Managed Cryptographic Object that is a digital certificate. It is a DER-encoded X.509 public key certificate.
 * Object Encoding REQUIRED Certificate Structure Certificate Type Enumeration Yes Certificate Value Byte String Yes
 */
public class Certificate extends KmipObject {

    @JsonProperty(value = "CertificateType")
    private CertificateType certificateType;

    @JsonProperty(value = "CertificateValue")
    private byte[] certificateValue;

    protected Certificate() {
    }

    public Certificate(CertificateType certificateType, byte[] certificateValue) {
        this.certificateType = certificateType;
        this.certificateValue = certificateValue;
    }

    public CertificateType getCertificateType() {
        return this.certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    public byte[] getCertificateValue() {
        return this.certificateValue;
    }

    public void setCertificateValue(byte[] certificateValue) {
        this.certificateValue = certificateValue;
    }

    public Certificate certificateType(CertificateType certificateType) {
        setCertificateType(certificateType);
        return this;
    }

    public Certificate certificateValue(byte[] certificateValue) {
        setCertificateValue(certificateValue);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Certificate)) {
            return false;
        }
        Certificate certificate = (Certificate) o;
        return Objects.equals(certificateType, certificate.certificateType)
            && Arrays.equals(certificateValue, certificate.certificateValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateType, certificateValue);
    }

    @Override
    public String toString() {
        return "{" + " certificateType='" + getCertificateType() + "'" + ", certificateValue='" + getCertificateValue()
            + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.Certificate;
    }

    /**
     * Deserialize an instance from its Json representation obtained using toJson()
     * 
     * @param json the JSON string
     * @return the {@link Certificate}
     * @throws CosmianException if the {@link Certificate} cannot be parsed
     */
    public static Certificate fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, Certificate.class);
    }
}
