package com.cosmian.rest.cover_crypt.data;

import java.util.Objects;

import com.cosmian.rest.cover_crypt.acccess_policy.Attr;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = DataToEncryptSerializer.class)
public class DataToEncrypt {

    private Attr[] policyAttributes;
    private byte[] data;

    public DataToEncrypt() {}

    public DataToEncrypt(Attr[] policyAttributes, byte[] data) {
        this.policyAttributes = policyAttributes;
        this.data = data;
    }

    public Attr[] getPolicyAttributes() {
        return this.policyAttributes;
    }

    public void setPolicyAttributes(Attr[] policyAttributes) {
        this.policyAttributes = policyAttributes;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public DataToEncrypt policyAttributes(Attr[] policyAttributes) {
        setPolicyAttributes(policyAttributes);
        return this;
    }

    public DataToEncrypt data(byte[] data) {
        setData(data);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DataToEncrypt)) {
            return false;
        }
        DataToEncrypt dataToEncrypt = (DataToEncrypt)o;
        return Objects.equals(policyAttributes, dataToEncrypt.policyAttributes)
            && Objects.equals(data, dataToEncrypt.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyAttributes, data);
    }

    @Override
    public String toString() {
        return "{" + " policyAttributes='" + getPolicyAttributes() + "'" + ", data='" + Arrays.toString(getData()) + "'" + "}";
    }

}
