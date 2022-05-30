package com.cosmian.rest.cover_crypt.data;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.rest.cover_crypt.acccess_policy.Attr;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = DataToEncryptSerializer.class)
public class DataToEncrypt {

    private Attr[] policyAttributes;

    private byte[] data;

    public DataToEncrypt() {
    }

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
        DataToEncrypt dataToEncrypt = (DataToEncrypt) o;
        return Arrays.equals(policyAttributes, dataToEncrypt.policyAttributes)
            && Arrays.equals(data, dataToEncrypt.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(policyAttributes), Arrays.hashCode(data));
    }

    @Override
    public String toString() {
        return "{" + " policyAttributes='" + Arrays.toString(getPolicyAttributes()) + "'" + ", data='"
            + Arrays.toString(getData()) + "'" + "}";
    }

}
