package com.cosmian.rest.kmip.data_structures;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.types.RecommendedCurve;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransparentECPublicKey implements KmipStruct {

    @JsonProperty(value = "RecommendedCurve")
    private RecommendedCurve recommendedCurve;

    @JsonProperty(value = "QString")
    private byte[] qString;

    public TransparentECPublicKey() {
    }

    public TransparentECPublicKey(RecommendedCurve recommendedCurve, byte[] qString) {
        this.recommendedCurve = recommendedCurve;
        this.qString = qString;
    }

    public RecommendedCurve getRecommendedCurve() {
        return this.recommendedCurve;
    }

    public void setRecommendedCurve(RecommendedCurve recommendedCurve) {
        this.recommendedCurve = recommendedCurve;
    }

    public byte[] getQString() {
        return this.qString;
    }

    public void setQString(byte[] qString) {
        this.qString = qString;
    }

    public TransparentECPublicKey recommendedCurve(RecommendedCurve recommendedCurve) {
        setRecommendedCurve(recommendedCurve);
        return this;
    }

    public TransparentECPublicKey qString(byte[] qString) {
        setQString(qString);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransparentECPublicKey)) {
            return false;
        }
        TransparentECPublicKey transparentECPublicKey = (TransparentECPublicKey) o;
        return Objects.equals(recommendedCurve, transparentECPublicKey.recommendedCurve)
            && Arrays.equals(qString, transparentECPublicKey.qString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommendedCurve, Arrays.hashCode(qString));
    }

    @Override
    public String toString() {
        return "{" + " recommendedCurve='" + getRecommendedCurve() + "'" + ", qString='" + Arrays.toString(getQString()) + "'" + "}";
    }

}
