package com.cosmian.jna.covercrypt.structs;

public class DecryptedHeader {

    private final byte[] symmetricKey;

    private final byte[] additionalData;

    public DecryptedHeader(byte[] symmetricKey, byte[] additional_data) {
        this.symmetricKey = symmetricKey;
        this.additionalData = additional_data;
    }

    public byte[] getSymmetricKey() {
        return this.symmetricKey;
    }

    public byte[] getAdditionalData() {
        return this.additionalData;
    }

}
