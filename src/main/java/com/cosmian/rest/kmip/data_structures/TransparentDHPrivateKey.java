package com.cosmian.rest.kmip.data_structures;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransparentDHPrivateKey implements KmipStruct {

    @JsonProperty(value = "P")
    private BigInteger p;

    @JsonProperty(value = "Q")
    private Optional<BigInteger> q;

    @JsonProperty(value = "G")
    private BigInteger g;

    @JsonProperty(value = "J")
    private Optional<BigInteger> j;

    @JsonProperty(value = "X")
    private BigInteger x;

    public TransparentDHPrivateKey() {}

    public TransparentDHPrivateKey(BigInteger p, Optional<BigInteger> q, BigInteger g, Optional<BigInteger> j,
        BigInteger x) {
        this.p = p;
        this.q = q;
        this.g = g;
        this.j = j;
        this.x = x;
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

    public BigInteger getX() {
        return this.x;
    }

    public void setX(BigInteger x) {
        this.x = x;
    }

    public TransparentDHPrivateKey p(BigInteger p) {
        setP(p);
        return this;
    }

    public TransparentDHPrivateKey q(Optional<BigInteger> q) {
        setQ(q);
        return this;
    }

    public TransparentDHPrivateKey g(BigInteger g) {
        setG(g);
        return this;
    }

    public TransparentDHPrivateKey j(Optional<BigInteger> j) {
        setJ(j);
        return this;
    }

    public TransparentDHPrivateKey x(BigInteger x) {
        setX(x);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransparentDHPrivateKey)) {
            return false;
        }
        TransparentDHPrivateKey transparentDHPrivateKey = (TransparentDHPrivateKey)o;
        return Objects.equals(p, transparentDHPrivateKey.p) && Objects.equals(q, transparentDHPrivateKey.q)
            && Objects.equals(g, transparentDHPrivateKey.g) && Objects.equals(j, transparentDHPrivateKey.j)
            && Objects.equals(x, transparentDHPrivateKey.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p, q, g, j, x);
    }

    @Override
    public String toString() {
        return "{" + " p='" + getP() + "'" + ", q='" + getQ() + "'" + ", g='" + getG() + "'" + ", j='" + getJ() + "'"
            + ", x='" + getX() + "'" + "}";
    }
}
