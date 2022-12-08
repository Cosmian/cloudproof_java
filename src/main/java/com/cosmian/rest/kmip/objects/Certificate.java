package com.cosmian.rest.kmip.objects;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.rest.kmip.types.CertificateType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.utils.CloudproofException;
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

    /**
     * Get the {@link CertificateType}
     * 
     * @return the {@link CertificateType}
     */
    public CertificateType getCertificateType() {
        return this.certificateType;
    }

    /**
     * Set the {@link CertificateType}
     * 
     * @param certificateType the type
     */
    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    /**
     * Get the {@link Certificate} value
     * 
     * @return the value
     */
    public byte[] getCertificateValue() {
        return this.certificateValue;
    }

    /**
     * Set the {@link Certificate} value
     * 
     * @param certificateValue the value
     */
    public void setCertificateValue(byte[] certificateValue) {
        this.certificateValue = certificateValue;
    }

    /**
     * Set the {@link CertificateType}
     * 
     * @param certificateType the {@link CertificateType}
     * @return the {@link Certificate}
     */
    public Certificate certificateType(CertificateType certificateType) {
        setCertificateType(certificateType);
        return this;
    }

    /**
     * Set the {@link Certificate} value
     * 
     * @param certificateValue the value
     * @return the {@link Certificate}
     */
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
     * @throws CloudproofException if the {@link Certificate} cannot be parsed
     */
    public static Certificate fromJson(String json) throws CloudproofException {
        return KmipObject.fromJson(json, Certificate.class);
    }
}
