package com.cosmian.rest.kmip.data_structures;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransparentDHPublicKey implements KmipStruct {

    @JsonProperty(value = "P")
    private BigInteger p;

    @JsonProperty(value = "Q")
    private Optional<BigInteger> q;

    @JsonProperty(value = "G")
    private BigInteger g;

    @JsonProperty(value = "J")
    private Optional<BigInteger> j;

    @JsonProperty(value = "Y")
    private BigInteger y;

    public TransparentDHPublicKey() {}

    public TransparentDHPublicKey(BigInteger p, Optional<BigInteger> q, BigInteger g, Optional<BigInteger> j,
        BigInteger y) {
        this.p = p;
        this.q = q;
        this.g = g;
        this.j = j;
        this.y = y;
    }

    public BigInteger getP() {
        return this.p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public Optional<BigInteger> getQ() {
        return this.q;
    }

    public void setQ(Optional<BigInteger> q) {
        this.q = q;
    }

    public BigInteger getG() {
        return this.g;
    }

    public void setG(BigInteger g) {
        this.g = g;
    }

    public Optional<BigInteger> getJ() {
        return this.j;
    }

    public void setJ(Optional<BigInteger> j) {
        this.j = j;
    }

    public BigInteger getY() {
        return this.y;
    }

    public void setY(BigInteger y) {
        this.y = y;
    }

    public TransparentDHPublicKey p(BigInteger p) {
        setP(p);
        return this;
    }

    public TransparentDHPublicKey q(Optional<BigInteger> q) {
        setQ(q);
        return this;
    }

    public TransparentDHPublicKey g(BigInteger g) {
        setG(g);
        return this;
    }

    public TransparentDHPublicKey j(Optional<BigInteger> j) {
        setJ(j);
        return this;
    }

    public TransparentDHPublicKey y(BigInteger y) {
        setY(y);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransparentDHPublicKey)) {
            return false;
        }
        TransparentDHPublicKey transparentDHPublicKey = (TransparentDHPublicKey)o;
        return Objects.equals(p, transparentDHPublicKey.p) && Objects.equals(q, transparentDHPublicKey.q)
            && Objects.equals(g, transparentDHPublicKey.g) && Objects.equals(j, transparentDHPublicKey.j)
            && Objects.equals(y, transparentDHPublicKey.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p, q, g, j, y);
    }

    @Override
    public String toString() {
        return "{" + " p='" + getP() + "'" + ", q='" + getQ() + "'" + ", g='" + getG() + "'" + ", j='" + getJ() + "'"
            + ", y='" + getY() + "'" + "}";
    }
}
