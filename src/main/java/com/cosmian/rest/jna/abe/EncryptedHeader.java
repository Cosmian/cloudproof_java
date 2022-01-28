package com.cosmian.rest.jna.abe;

public class EncryptedHeader {

    private final byte[] symmetricKey;
    private final byte[] headerBytes;

    public EncryptedHeader(byte[] symmetricKey, byte[] headerBytes) {
        this.symmetricKey = symmetricKey;
        this.headerBytes = headerBytes;
    }

    public byte[] getSymmetricKey() {
        return this.symmetricKey;
    }

    public byte[] getHeaderBytes() {
        return this.headerBytes;
    }

}
