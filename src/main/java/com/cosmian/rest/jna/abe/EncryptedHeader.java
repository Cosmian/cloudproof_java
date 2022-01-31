package com.cosmian.rest.jna.abe;

public class EncryptedHeader {

    private final byte[] symmetricKey;
    private final byte[] encryptedHeaderBytes;

    public EncryptedHeader(byte[] symmetricKey, byte[] headerBytes) {
        this.symmetricKey = symmetricKey;
        this.encryptedHeaderBytes = headerBytes;
    }

    public byte[] getSymmetricKey() {
        return this.symmetricKey;
    }

    public byte[] getEncryptedHeaderBytes() {
        return this.encryptedHeaderBytes;
    }

}
