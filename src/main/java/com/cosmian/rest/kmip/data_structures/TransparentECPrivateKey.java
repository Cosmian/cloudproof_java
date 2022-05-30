package com.cosmian.rest.kmip.data_structures;

import java.math.BigInteger;
import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.types.RecommendedCurve;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransparentECPrivateKey implements KmipStruct {

    @JsonProperty(value = "RecommendedCurve")
    private RecommendedCurve recommendedCurve;

    @JsonProperty(value = "D")
    private BigInteger d;

    public TransparentECPrivateKey() {
    }

    public TransparentECPrivateKey(RecommendedCurve recommendedCurve, BigInteger d) {
        this.recommendedCurve = recommendedCurve;
        this.d = d;
    }

    public RecommendedCurve getRecommendedCurve() {
        return this.recommendedCurve;
    }

    public void setRecommendedCurve(RecommendedCurve recommendedCurve) {
        this.recommendedCurve = recommendedCurve;
    }

    public BigInteger getD() {
        return this.d;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }

    public TransparentECPrivateKey recommendedCurve(RecommendedCurve recommendedCurve) {
        setRecommendedCurve(recommendedCurve);
        return this;
    }

    public TransparentECPrivateKey d(BigInteger d) {
        setD(d);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransparentECPrivateKey)) {
            return false;
        }
        TransparentECPrivateKey transparentECPrivateKey = (TransparentECPrivateKey) o;
        return Objects.equals(recommendedCurve, transparentECPrivateKey.recommendedCurve)
            && Objects.equals(d, transparentECPrivateKey.d);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommendedCurve, d);
    }

    @Override
    public String toString() {
        return "{" + " recommendedCurve='" + getRecommendedCurve() + "'" + ", d='" + getD() + "'" + "}";
    }

}
