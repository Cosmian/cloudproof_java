package com.cosmian.cover_crypt;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSecretKeyTestVector {
    static final CoverCrypt coverCrypt = new CoverCrypt();

    @JsonProperty("access_policy")
    private String accessPolicy;

    @JsonProperty("key")
    private byte[] key;

    public byte[] getKey() {
        return key;
    }

    public String getAccessPolicy() {
        return accessPolicy;
    }

    public static UserSecretKeyTestVector generate(byte[] masterSecretKey,
                                                   Policy policy,
                                                   String accessPolicy)
        throws CloudproofException {
        byte[] key = coverCrypt.generateUserPrivateKey(masterSecretKey,
            accessPolicy, policy);
        UserSecretKeyTestVector userKey = new UserSecretKeyTestVector();
        userKey.accessPolicy = accessPolicy;
        userKey.key = key;
        return userKey;
    }
}
