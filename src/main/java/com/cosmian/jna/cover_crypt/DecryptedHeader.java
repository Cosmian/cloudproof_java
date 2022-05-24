package com.cosmian.jna.cover_crypt;

public class DecryptedHeader {

    private final byte[] symmetricKey;
    private final byte[] uid;
    private final byte[] additionalData;

    public DecryptedHeader(byte[] symmetricKey, byte[] uid, byte[] additional_data) {
        this.symmetricKey = symmetricKey;
        this.uid = uid;
        this.additionalData = additional_data;
    }

    public byte[] getSymmetricKey() {
        return this.symmetricKey;
    }

    public byte[] getUid() {
        return this.uid;
    }

    public byte[] getAdditionalData() {
        return this.additionalData;
    }

}
