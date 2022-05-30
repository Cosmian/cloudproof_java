package com.cosmian.rest.kmip.types;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Cryptographic Domain Parameters attribute (4.14) is a structure that contains fields that MAY need to be
 * specified in the Create Key Pair Request Payload. Specific fields MAY only pertain to certain types of Managed
 * Cryptographic Objects. T he domain parameter q_length corresponds to the bit length of parameter Q (refer to
 * [RFC7778],[SEC2]and [SP800-56A]). q_length applies to algorithms such as DSA and DH. The bit length of parameter P
 * (refer toto [RFC7778],[SEC2]and [SP800-56A]) is specified separately by setting the Cryptographic Length attribute.
 * Recommended Curve is applicable to elliptic curve algorithms such as ECDSA, ECDH, and ECMQV
 */
public class CryptographicDomainParameters {

    @JsonProperty(value = "Qlength")
    private Optional<Integer> q_length = Optional.empty();

    @JsonProperty(value = "RecommendedCurve")
    private Optional<RecommendedCurve> recommended_curve = Optional.empty();

    public static CryptographicDomainParameters empty() {
        return new CryptographicDomainParameters();
    }
}
