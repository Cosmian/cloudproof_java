package com.cosmian.cover_crypt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EncryptionTestVector {
    static final CoverCrypt coverCrypt = new CoverCrypt();

    @JsonProperty("encryption_policy")
    private String encryptionPolicy;

    @JsonProperty("plaintext")
    private byte[] plaintext;

    @JsonProperty("ciphertext")
    private byte[] ciphertext;

    @JsonProperty("header_metadata")
    private byte[] headerMetadata;

    @JsonProperty("authentication_data")
    private byte[] authenticationData;

    public void decrypt(byte[] key) throws CloudproofException {
        DecryptedData res = coverCrypt.decrypt(key, ciphertext, authenticationData);

        // Verify everything is correct
        assertTrue(Arrays.equals(this.plaintext, res.getPlaintext()));
        assertTrue(Arrays.equals(this.headerMetadata, res.getHeaderMetaData()));
    }

    public static EncryptionTestVector generate(Policy policy,
                                                byte[] publicKey,
                                                String encryptionPolicy,
                                                String plaintext,
                                                byte[] headerMetadata,
                                                byte[] authenticationData)
        throws CloudproofException {

        EncryptionTestVector out = new EncryptionTestVector();
        out.plaintext = plaintext.getBytes();
        out.ciphertext = coverCrypt.encrypt(policy, publicKey,
            encryptionPolicy,
            out.plaintext,
            authenticationData, headerMetadata);
        out.headerMetadata = headerMetadata;
        out.authenticationData = authenticationData;
        out.encryptionPolicy = encryptionPolicy;
        return out;
    }
}
