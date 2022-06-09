package com.cosmian.jna.cover_crypt;

public class MasterKeys {

    private final byte[] privateKey;

    private final byte[] publicKey;

    public MasterKeys(byte[] privateKey, byte[] publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
