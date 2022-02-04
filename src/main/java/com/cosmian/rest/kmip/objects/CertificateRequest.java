package com.cosmian.rest.kmip.objects;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.types.CertificateRequestType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CertificateRequest extends KmipObject {

    @JsonProperty(value = "CertificateRequestType")
    private CertificateRequestType certificate_request_type;

    @JsonProperty(value = "CertificateRequestValue")
    private byte[] certificate_request_value;

    public CertificateRequest() {
    }

    public CertificateRequest(CertificateRequestType certificate_request_type, byte[] certificate_request_value) {
        this.certificate_request_type = certificate_request_type;
        this.certificate_request_value = certificate_request_value;
    }

    public CertificateRequestType getCertificate_request_type() {
        return this.certificate_request_type;
    }

    public void setCertificate_request_type(CertificateRequestType certificate_request_type) {
        this.certificate_request_type = certificate_request_type;
    }

    public byte[] getCertificate_request_value() {
        return this.certificate_request_value;
    }

    public void setCertificate_request_value(byte[] certificate_request_value) {
        this.certificate_request_value = certificate_request_value;
    }

    public CertificateRequest certificate_request_type(CertificateRequestType certificate_request_type) {
        setCertificate_request_type(certificate_request_type);
        return this;
    }

    public CertificateRequest certificate_request_value(byte[] certificate_request_value) {
        setCertificate_request_value(certificate_request_value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CertificateRequest)) {
            return false;
        }
        CertificateRequest certificateRequest = (CertificateRequest) o;
        return Objects.equals(certificate_request_type, certificateRequest.certificate_request_type)
                && Arrays.equals(certificate_request_value, certificateRequest.certificate_request_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificate_request_type, certificate_request_value);
    }

    @Override
    public String toString() {
        return "{" + " certificate_request_type='" + getCertificate_request_type() + "'"
                + ", certificate_request_value='" + getCertificate_request_value() + "'" + "}";
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.Certificate_Request;
    }

    /**
     * 
     * Deserialize an instance from its Json representation obtained using
     * toJson()
     * 
     * @param json the JSON string
     * @return the {@link CertificateRequest}
     * @throws CosmianException if the parsing fails
     */
    public static CertificateRequest fromJson(String json) throws CosmianException {
        return KmipObject.fromJson(json, CertificateRequest.class);
    }
}
