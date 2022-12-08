package com.cosmian.jna.covercrypt.structs;

public class DecryptedHeader {

    private final byte[] symmetricKey;

    private final byte[] authenticationData;

    private final byte[] additionalData;

    public DecryptedHeader(byte[] symmetricKey, byte[] authenticationData, byte[] additional_data) {
        this.symmetricKey = symmetricKey;
        this.authenticationData = authenticationData;
        this.additionalData = additional_data;
    }

    public byte[] getSymmetricKey() {
        return this.symmetricKey;
    }

    public byte[] getUid() {
        return this.authenticationData;
    }

    public byte[] getAdditionalData() {
        return this.additionalData;
    }

}
